package com.ian.web.employee.leave;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.config.security.Roles;
import com.ian.web.employee.Employee;
import com.ian.web.fileupload.FileDTO;
import com.ian.web.fileupload.StorageService;
import com.ian.web.notification.Notifier;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;

/**
 * CR Request ID 016: multi-stage leave decision flow.
 *
 * Employee submits → HR screens → Supervisor endorsement → duration check:
 * up to 5 working days HR approves directly; over 5 days supporting documents
 * are required and the request goes through Administrative Review (plus
 * Council Review when over 15 days) before final approval by the Vice-Mayor.
 * Every step is audit-trailed and the employee is notified in-app. The ledger
 * deduction still only ever happens at APPROVED (LeaveLedger.syncLedgerEntry).
 *
 * CR 016 v2: the Supervisor, Secretary to the City Council and Vice-Mayor act
 * on their own accounts (ROLE_SUPERVISOR / ROLE_COUNCIL / ROLE_VICEMAYOR) —
 * each may act only while the application sits at their stage and is notified
 * when a request enters their queue. ADMIN/HR retain the v1 record-any-stage
 * fallback (also covers the Chief Admin Officer stage, which has no account,
 * and acting officials — the signatory name stays editable per action).
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class LeaveWorkflowController {

	private static final DecimalFormat CREDIT_FMT = new DecimalFormat("0.###");
	private static final DateTimeFormatter ACTION_TIME = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");

	private final LeaveApplicationRepository leaveApplicationRepository;
	private final LeaveWorkflowActionRepository leaveWorkflowActionRepository;
	private final LeaveSignatoryRepository leaveSignatoryRepository;
	private final LeaveLedger leaveLedger;
	private final StorageService storageService;
	private final Notifier notifier;

	// ── staff: workflow transitions ──────────────────────────────────────────

	@PostMapping("/leave-workflow/{id}/action")
	@Transactional
	public String performAction(@PathVariable long id,
			@RequestParam("action") String action,
			@RequestParam(value = "signatoryName", required = false) String signatoryName,
			@RequestParam(value = "signatoryTitle", required = false) String signatoryTitle,
			@RequestParam(value = "remarks", required = false) String remarks,
			HttpServletRequest request, final RedirectAttributes redirect) {

		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		Employee actor = sessionActor(request);
		String back = "redirect:/leaves/" + app.getEmployee().getId() + "/" + app.getEmployee().getEmpHashCode();

		if (!mayActOn(actor, app)) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR",
					"You are not allowed to act on this application at its current stage."));
			return Roles.isStaff(actor) ? back : "redirect:/leave-approvals";
		}
		String error = validateAction(app, actor, action, signatoryName, remarks);
		if (error != null) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", error));
			return back;
		}

		String fromStatus = app.getStatus();
		String toStatus = targetStatus(app, action);
		app.setStatus(toStatus);

		if (LeaveConstants.ACTION_ENDORSE.equals(action)) {
			// The supervisor recorded here is the endorsement signatory printed on Form 6.
			app.setEndorserName(signatoryName);
			app.setEndorserTitle(nvl(signatoryTitle));
		}
		if (LeaveConstants.ACTION_DISAPPROVE.equals(action)) {
			app.setDisapprovalReason(remarks);
		}
		if (LeaveConstants.STATUS_APPROVED.equals(toStatus) && app.getApprovedDaysWithPay() == null) {
			app.setApprovedDaysWithPay(app.getWorkingDays());
		}

		leaveLedger.fillCertification(app);
		LeaveApplication saved = leaveApplicationRepository.save(app);
		leaveLedger.syncLedgerEntry(saved);

		recordAction(saved, fromStatus, toStatus, action, actor, signatoryName, signatoryTitle, remarks);
		notifier.notify(saved.getEmployee(), notificationMessage(saved, action, toStatus, remarks),
				myLeavesLink(saved.getEmployee()));
		notifyStageActors(saved, toStatus);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", actionLabel(action) + " recorded."));
		return Roles.isStaff(actor) ? back : "redirect:/leave-approvals";
	}

	/** CR 016 v2: alert the accounts whose stage the application just entered. */
	private void notifyStageActors(LeaveApplication app, String toStatus) {
		String role = stageRole(toStatus);
		if (role == null) {
			return;
		}
		Employee employee = app.getEmployee();
		String stage = LeaveConstants.STATUS_FOR_ENDORSEMENT.equals(toStatus) ? "endorsement"
				: LeaveConstants.STATUS_FOR_COUNCIL_REVIEW.equals(toStatus) ? "Council Review"
				: "final approval";
		notifier.notifyRole(role,
				"Leave application of " + buildCardName(employee) + " (" + nvl(app.getLeaveType())
						+ ", " + app.getInclusiveDatesDisplay() + ", "
						+ CREDIT_FMT.format(workingDays(app)) + " day(s)) awaits your " + stage + ".",
				"/leaves/" + employee.getId() + "/" + employee.getEmpHashCode());
	}

	/** The dedicated account role that owns a workflow status, or null when HR does. */
	static String stageRole(String status) {
		switch (nvl(status)) {
			case LeaveConstants.STATUS_FOR_ENDORSEMENT: return Roles.SUPERVISOR;
			case LeaveConstants.STATUS_FOR_COUNCIL_REVIEW: return Roles.COUNCIL;
			case LeaveConstants.STATUS_FOR_FINAL_APPROVAL: return Roles.VICEMAYOR;
			default: return null;
		}
	}

	/**
	 * CR 016 v2 role gate: a dedicated actor may act only while the application
	 * sits at their stage; ADMIN/HR may record every stage (v1 behavior).
	 */
	static boolean mayActOn(Employee actor, LeaveApplication app) {
		if (actor == null || app == null) {
			return false;
		}
		if (Roles.isStaff(actor)) {
			return true;
		}
		return nvl(app.getStatus()).equals(actorStage(actor));
	}

	/** The single workflow status a dedicated actor account is in charge of. */
	private static String actorStage(Employee actor) {
		if (Roles.hasRole(actor, Roles.SUPERVISOR)) return LeaveConstants.STATUS_FOR_ENDORSEMENT;
		if (Roles.hasRole(actor, Roles.COUNCIL)) return LeaveConstants.STATUS_FOR_COUNCIL_REVIEW;
		if (Roles.hasRole(actor, Roles.VICEMAYOR)) return LeaveConstants.STATUS_FOR_FINAL_APPROVAL;
		return null;
	}

	/** Statuses whose applications belong in this actor's pending queue. */
	static List<String> actionableStatusesFor(Employee actor) {
		if (Roles.isStaff(actor)) {
			return LeaveConstants.PENDING_STATUSES;
		}
		String stage = actorStage(actor);
		return stage == null ? List.of() : List.of(stage);
	}

	/** Actions offered to this actor for the application's current status. */
	List<String> allowedActionsFor(LeaveApplication app, Employee actor) {
		return mayActOn(actor, app) ? allowedActions(app) : List.of();
	}

	/** Session actor, falling back to the authenticated principal. */
	private static Employee sessionActor(HttpServletRequest request) {
		Employee actor = (Employee) request.getSession().getAttribute("actorObj");
		if (actor != null) {
			return actor;
		}
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		return auth != null && auth.getPrincipal() instanceof Employee ? (Employee) auth.getPrincipal() : null;
	}

	/** HR attaches the supporting documents received for a > 5-day leave. */
	@PostMapping("/leave-workflow/{id}/upload-docs")
	@Transactional
	public String uploadSupportingDocs(@PathVariable long id,
			@RequestParam("supportingDocs") MultipartFile[] files,
			HttpServletRequest request, final RedirectAttributes redirect) {

		// Managed entity: @ElementCollection updates are unreliable on detached merges.
		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		String back = "redirect:/leaves/" + app.getEmployee().getId() + "/" + app.getEmployee().getEmpHashCode();

		// CR 016 v2: documents are received and attached by HR, not by reviewer accounts.
		if (!Roles.isStaff(sessionActor(request))) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR",
					"Only HR can attach supporting documents."));
			return back;
		}

		int uploaded = storeSupportingDocs(app, files);
		if (uploaded == 0) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "No file selected."));
			return back;
		}
		leaveApplicationRepository.save(app);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS",
				uploaded + " supporting document(s) attached."));
		return back;
	}

	/** Uploads the given files and appends their URLs; returns how many were stored. */
	public int storeSupportingDocs(LeaveApplication app, MultipartFile[] files) {
		if (files == null) {
			return 0;
		}
		List<String> urls = app.getSupportingDocUrls() != null
				? new ArrayList<>(app.getSupportingDocUrls()) : new ArrayList<>();
		int uploaded = 0;
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getOriginalFilename();
			if (name == null || name.isEmpty()) {
				continue;
			}
			try {
				int dot = name.lastIndexOf('.');
				String ext = dot >= 0 ? name.substring(dot) : "";
				String fileName = "leave_doc_" + app.getEmployee().getId() + "_" + i + "_"
						+ System.currentTimeMillis() + ext;
				FileDTO fileDTO = storageService.uploadFile(files[i], fileName);
				urls.add(fileDTO.getDownloadUri());
				uploaded++;
			} catch (Exception e) {
				log.error("Failed to upload leave supporting document", e);
			}
		}
		app.setSupportingDocUrls(urls);
		return uploaded;
	}

	// ── staff: workflow panel JSON ───────────────────────────────────────────

	@GetMapping("/leave-workflow/{id}/panel")
	@ResponseBody
	public Map<String, Object> workflowPanel(@PathVariable long id, HttpServletRequest request) {
		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		Employee actor = sessionActor(request);
		LeaveSignatory signatory = leaveSignatoryRepository.findAll().stream()
				.findFirst().orElseGet(LeaveSignatory::new);

		Map<String, Object> panel = new LinkedHashMap<>();
		panel.put("id", app.getId());
		panel.put("status", app.getStatus());
		panel.put("workingDays", app.getWorkingDays());
		panel.put("needsDocs", needsDocs(app));
		panel.put("hasDocs", hasDocs(app));
		panel.put("docs", docList(app));
		panel.put("canUploadDocs", Roles.isStaff(actor));

		// CR 016 v2: a dedicated actor signing at their own stage defaults to
		// their own name; the field remains editable for acting officials.
		boolean actorOwnsStage = !Roles.isStaff(actor)
				&& nvl(app.getStatus()).equals(actorStage(actor));

		List<Map<String, Object>> actions = new ArrayList<>();
		for (String action : allowedActionsFor(app, actor)) {
			Map<String, Object> a = new LinkedHashMap<>();
			a.put("action", action);
			a.put("label", actionLabel(action));
			a.put("requiresSignatory", requiresSignatory(action));
			a.put("requiresRemarks", requiresRemarks(action));
			a.put("signatoryName", actorOwnsStage && requiresSignatory(action)
					? buildCardName(actor) : suggestedSignatoryName(action, signatory));
			a.put("signatoryTitle", actorOwnsStage && requiresSignatory(action)
					? actorTitle(actor) : suggestedSignatoryTitle(action, signatory));
			actions.add(a);
		}
		panel.put("allowedActions", actions);

		List<Map<String, Object>> history = new ArrayList<>();
		for (LeaveWorkflowAction a : leaveWorkflowActionRepository
				.findByLeaveApplicationIdOrderByCreatedAtAscIdAsc(id)) {
			Map<String, Object> h = new LinkedHashMap<>();
			h.put("action", a.getAction());
			h.put("label", actionLabel(a.getAction()));
			h.put("fromStatus", nvl(a.getFromStatus()));
			h.put("toStatus", nvl(a.getToStatus()));
			h.put("actorName", nvl(a.getActorName()));
			h.put("signatoryName", nvl(a.getSignatoryName()));
			h.put("signatoryTitle", nvl(a.getSignatoryTitle()));
			h.put("remarks", nvl(a.getRemarks()));
			h.put("createdAt", a.getCreatedAt() != null ? ACTION_TIME.format(a.getCreatedAt()) : "");
			history.add(h);
		}
		panel.put("history", history);
		return panel;
	}

	// ── employee: cancel / appeal (CR 016) ───────────────────────────────────

	@PostMapping("/cancelMyLeave/{id}")
	@Transactional
	public String cancelMyLeave(@PathVariable long id, HttpServletRequest request,
			final RedirectAttributes redirect) {

		Employee actor = (Employee) request.getSession().getAttribute("actorObj");
		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		if (actor == null || app.getEmployee() == null || actor.getId() != app.getEmployee().getId()) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return actor == null ? "redirect:/login" : "redirect:" + myLeavesLink(actor);
		}
		if (!LeaveConstants.PENDING_STATUSES.contains(app.getStatus())) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR",
					"Only pending applications can be cancelled."));
			return "redirect:" + myLeavesLink(actor);
		}

		String fromStatus = app.getStatus();
		app.setStatus(LeaveConstants.STATUS_CANCELLED);
		LeaveApplication saved = leaveApplicationRepository.save(app);
		leaveLedger.syncLedgerEntry(saved);
		recordAction(saved, fromStatus, LeaveConstants.STATUS_CANCELLED, LeaveConstants.ACTION_CANCEL,
				actor, null, null, null);
		notifier.notify(saved.getEmployee(),
				"Your " + leaveLabel(saved) + " has been cancelled as you requested.",
				myLeavesLink(saved.getEmployee()));

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Leave application cancelled."));
		return "redirect:" + myLeavesLink(actor);
	}

	@PostMapping("/appealMyLeave/{id}")
	@Transactional
	public String appealMyLeave(@PathVariable long id,
			@RequestParam(value = "remarks", required = false) String remarks,
			HttpServletRequest request, final RedirectAttributes redirect) {

		Employee actor = (Employee) request.getSession().getAttribute("actorObj");
		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		if (actor == null || app.getEmployee() == null || actor.getId() != app.getEmployee().getId()) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return actor == null ? "redirect:/login" : "redirect:" + myLeavesLink(actor);
		}
		if (!LeaveConstants.STATUS_DISAPPROVED.equals(app.getStatus())) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR",
					"Only disapproved applications can be appealed."));
			return "redirect:" + myLeavesLink(actor);
		}

		app.setStatus(LeaveConstants.STATUS_APPEALED);
		LeaveApplication saved = leaveApplicationRepository.save(app);
		recordAction(saved, LeaveConstants.STATUS_DISAPPROVED, LeaveConstants.STATUS_APPEALED,
				LeaveConstants.ACTION_APPEAL, actor, null, null, remarks);
		notifier.notify(saved.getEmployee(),
				"Your appeal for the " + leaveLabel(saved) + " has been submitted for HR screening.",
				myLeavesLink(saved.getEmployee()));
		// CR 016 v2: alert HR that an appeal re-entered the screening queue.
		notifier.notifyRole(Roles.HR, "Appeal filed by " + buildCardName(saved.getEmployee())
				+ " on the " + leaveLabel(saved) + " awaits HR screening.", "/leave-applications");

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Appeal submitted."));
		return "redirect:" + myLeavesLink(actor);
	}

	// ── verification receipt PDF (printed for > 5-day leaves) ────────────────

	@GetMapping("/leaveVerificationReceiptPdf/{applicationId}")
	@Transactional
	public void exportVerificationReceiptPdf(@PathVariable long applicationId,
			HttpServletRequest request, HttpServletResponse response) throws Exception {

		LeaveApplication app = leaveApplicationRepository.findById(applicationId).orElseThrow();
		Employee employee = app.getEmployee();
		Employee actor = (Employee) request.getSession().getAttribute("actorObj");

		Map<String, Object> map = new HashMap<>();
		map.put("RECEIPT_NO", String.format("LV-%06d", app.getId()));
		map.put("EMP_NAME", employee != null ? buildCardName(employee) : "");
		map.put("OFFICE_DEPARTMENT", nvl(app.getOfficeDepartment()));
		map.put("POSITION", nvl(app.getPosition()));
		map.put("LEAVE_TYPE", nvl(app.getLeaveType()));
		map.put("DATE_OF_FILING", app.getDateOfFiling() != null
				? LeaveConstants.CARD_DATE.format(app.getDateOfFiling()) : "");
		map.put("INCLUSIVE_DATES", app.getInclusiveDatesDisplay());
		map.put("WORKING_DAYS", app.getWorkingDays() != null ? CREDIT_FMT.format(app.getWorkingDays()) : "");
		map.put("STATUS", nvl(app.getStatus()));
		map.put("ENDORSER_NAME", nvl(app.getEndorserName()));
		map.put("ENDORSER_TITLE", nvl(app.getEndorserTitle()));

		StringBuilder docs = new StringBuilder();
		int n = 1;
		if (app.getSupportingDocUrls() != null) {
			for (String url : app.getSupportingDocUrls()) {
				docs.append(n++).append(". ").append(fileNameOf(url)).append("\n");
			}
		}
		map.put("DOCUMENT_LIST", docs.length() > 0 ? docs.toString() : "(none)");
		String verifiedBy = nvl(app.getVerifierName());
		map.put("VERIFIED_BY", !verifiedBy.isEmpty() ? verifiedBy
				: (actor != null ? buildCardName(actor) : ""));
		String verifiedByTitle = nvl(app.getVerifierTitle());
		map.put("VERIFIED_BY_TITLE", !verifiedByTitle.isEmpty() ? verifiedByTitle : "HR / Administrative Division");
		map.put("VERIFIED_AT", ACTION_TIME.format(LocalDateTime.now()));

		recordAction(app, null, null, LeaveConstants.ACTION_RECEIPT_PRINTED, actor, null, null, null);

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition",
				"inline; filename=Leave-Verification-Receipt-" + applicationId + ".pdf");
		InputStream reportStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("jasper/reports/Leave-Verification-Receipt.jasper");
		JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map,
				new JREmptyDataSource());
	}

	// ── transition rules ─────────────────────────────────────────────────────

	/** Actions staff may take from the application's current status. */
	List<String> allowedActions(LeaveApplication app) {
		String status = nvl(app.getStatus());
		switch (status) {
			case LeaveConstants.STATUS_FILED:
			case LeaveConstants.STATUS_APPEALED:
				return List.of(LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR,
						LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_RETURN);
			case LeaveConstants.STATUS_RETURNED:
				return List.of(LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR, LeaveConstants.ACTION_ENDORSE);
			case LeaveConstants.STATUS_FOR_ENDORSEMENT:
				// CR 020's "Supervisor must have approve, disapprove, return to HR
				// and endorse" reads "approve" as endorse, not a separate terminal
				// action — the CR's own next sentence says HR creates a receipt
				// "for the next approver to validate," meaning more approval
				// stages still follow the supervisor's decision.
				return List.of(LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_DISAPPROVE,
						LeaveConstants.ACTION_RETURN);
			case LeaveConstants.STATUS_ENDORSED:
				return workingDays(app) <= LeaveConstants.SHORT_PATH_MAX_DAYS
						? List.of(LeaveConstants.ACTION_APPROVE, LeaveConstants.ACTION_DISAPPROVE)
						: List.of(LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_FOR_ADMIN_REVIEW:
				return List.of(LeaveConstants.ACTION_ADMIN_ENDORSE, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_FOR_COUNCIL_REVIEW:
				return workingDays(app) >= LeaveConstants.COUNCIL_THRESHOLD_DAYS
						? List.of(LeaveConstants.ACTION_COUNCIL_ENDORSE, LeaveConstants.ACTION_DISAPPROVE)
						: List.of(LeaveConstants.ACTION_COUNCIL_APPROVE, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_FOR_FINAL_APPROVAL:
				return List.of(LeaveConstants.ACTION_FINAL_APPROVE, LeaveConstants.ACTION_DISAPPROVE,
						LeaveConstants.ACTION_RETURN);
			case LeaveConstants.STATUS_APPROVED:
				return List.of(LeaveConstants.ACTION_REOPEN);
			default:
				return List.of();
		}
	}

	/** Returns an error message, or null when the action is valid for this actor and application. */
	private String validateAction(LeaveApplication app, Employee actor, String action,
			String signatoryName, String remarks) {
		if (!allowedActionsFor(app, actor).contains(action)) {
			return "Action not allowed while the application is " + nvl(app.getStatus()) + ".";
		}
		if (requiresSignatory(action) && isBlank(signatoryName)) {
			return "Signatory name is required for this step.";
		}
		if (requiresRemarks(action) && isBlank(remarks)) {
			return LeaveConstants.ACTION_DISAPPROVE.equals(action)
					? "Please state the reason for disapproval."
					: "Please state the reason for returning the application.";
		}
		if (LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW.equals(action) && !hasDocs(app)) {
			return "Supporting documents are required for leaves of more than "
					+ (int) LeaveConstants.SHORT_PATH_MAX_DAYS + " working days. Attach them first.";
		}
		return null;
	}

	private String targetStatus(LeaveApplication app, String action) {
		switch (action) {
			case LeaveConstants.ACTION_RETURN:
				return LeaveConstants.STATUS_RETURNED;
			case LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR:
				return LeaveConstants.STATUS_FOR_ENDORSEMENT;
			case LeaveConstants.ACTION_ENDORSE:
				return LeaveConstants.STATUS_ENDORSED;
			case LeaveConstants.ACTION_APPROVE:
			case LeaveConstants.ACTION_FINAL_APPROVE:
			case LeaveConstants.ACTION_COUNCIL_APPROVE:
				return LeaveConstants.STATUS_APPROVED;
			case LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW:
				return LeaveConstants.STATUS_FOR_ADMIN_REVIEW;
			case LeaveConstants.ACTION_ADMIN_ENDORSE:
				// Every docs-required leave passes through Council Review; Council
				// either finalizes it herself or endorses on to the Vice-Mayor
				// depending on COUNCIL_THRESHOLD_DAYS (see allowedActions).
				return LeaveConstants.STATUS_FOR_COUNCIL_REVIEW;
			case LeaveConstants.ACTION_COUNCIL_ENDORSE:
				return LeaveConstants.STATUS_FOR_FINAL_APPROVAL;
			case LeaveConstants.ACTION_DISAPPROVE:
				return LeaveConstants.STATUS_DISAPPROVED;
			case LeaveConstants.ACTION_REOPEN:
				return LeaveConstants.STATUS_ENDORSED;
			default:
				throw new IllegalArgumentException("Unknown workflow action: " + action);
		}
	}

	static boolean requiresSignatory(String action) {
		return LeaveConstants.ACTION_ENDORSE.equals(action)
				|| LeaveConstants.ACTION_ADMIN_ENDORSE.equals(action)
				|| LeaveConstants.ACTION_COUNCIL_ENDORSE.equals(action)
				|| LeaveConstants.ACTION_COUNCIL_APPROVE.equals(action)
				|| LeaveConstants.ACTION_FINAL_APPROVE.equals(action);
	}

	static boolean requiresRemarks(String action) {
		return LeaveConstants.ACTION_RETURN.equals(action)
				|| LeaveConstants.ACTION_DISAPPROVE.equals(action);
	}

	static String actionLabel(String action) {
		switch (nvl(action)) {
			case LeaveConstants.ACTION_RETURN: return "Return for Corrections";
			case LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR: return "Forward to Supervisor (HR Screening)";
			case LeaveConstants.ACTION_ENDORSE: return "Supervisor Endorsement";
			case LeaveConstants.ACTION_APPROVE: return "Approve (HR)";
			case LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW: return "Forward for Administrative Review";
			case LeaveConstants.ACTION_ADMIN_ENDORSE: return "Administrative Review Passed";
			case LeaveConstants.ACTION_COUNCIL_ENDORSE: return "Council Review Passed";
			case LeaveConstants.ACTION_COUNCIL_APPROVE: return "Council Final Approval";
			case LeaveConstants.ACTION_FINAL_APPROVE: return "Final Approval (Vice-Mayor)";
			case LeaveConstants.ACTION_DISAPPROVE: return "Disapprove";
			case LeaveConstants.ACTION_CANCEL: return "Cancelled by Employee";
			case LeaveConstants.ACTION_APPEAL: return "Appealed by Employee";
			case LeaveConstants.ACTION_REOPEN: return "Reopened (approval reversed)";
			case LeaveConstants.ACTION_RECEIPT_PRINTED: return "Verification Receipt Printed";
			default: return nvl(action);
		}
	}

	private static String suggestedSignatoryName(String action, LeaveSignatory s) {
		switch (nvl(action)) {
			case LeaveConstants.ACTION_ENDORSE: return nvl(s.getEndorserName());
			case LeaveConstants.ACTION_ADMIN_ENDORSE: return nvl(s.getCertOfficerName());
			case LeaveConstants.ACTION_COUNCIL_ENDORSE:
			case LeaveConstants.ACTION_COUNCIL_APPROVE:
				return nvl(s.getRecommenderName());
			case LeaveConstants.ACTION_FINAL_APPROVE: return nvl(s.getApproverName());
			default: return "";
		}
	}

	private static String suggestedSignatoryTitle(String action, LeaveSignatory s) {
		switch (nvl(action)) {
			case LeaveConstants.ACTION_ENDORSE: return nvl(s.getEndorserTitle());
			case LeaveConstants.ACTION_ADMIN_ENDORSE: return nvl(s.getCertOfficerTitle());
			case LeaveConstants.ACTION_COUNCIL_ENDORSE:
			case LeaveConstants.ACTION_COUNCIL_APPROVE:
				return nvl(s.getRecommenderTitle());
			case LeaveConstants.ACTION_FINAL_APPROVE: return nvl(s.getApproverTitle());
			default: return "";
		}
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private void recordAction(LeaveApplication app, String fromStatus, String toStatus, String action,
			Employee actor, String signatoryName, String signatoryTitle, String remarks) {
		LeaveWorkflowAction row = new LeaveWorkflowAction();
		row.setLeaveApplication(app);
		row.setFromStatus(fromStatus);
		row.setToStatus(toStatus);
		row.setAction(action);
		row.setActorUsername(actor != null ? actor.getUsername() : "");
		row.setActorName(actor != null ? buildCardName(actor) : "");
		row.setSignatoryName(signatoryName);
		row.setSignatoryTitle(signatoryTitle);
		row.setRemarks(remarks);
		row.setCreatedAt(LocalDateTime.now());
		leaveWorkflowActionRepository.save(row);
	}

	private String notificationMessage(LeaveApplication app, String action, String toStatus, String remarks) {
		String label = leaveLabel(app);
		switch (action) {
			case LeaveConstants.ACTION_RETURN:
				return "Your " + label + " was returned for further review" + reasonSuffix(remarks);
			case LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR:
				return "Your " + label + " passed HR screening and awaits your supervisor's endorsement.";
			case LeaveConstants.ACTION_ENDORSE:
				return "Your " + label + " has been endorsed by your supervisor.";
			case LeaveConstants.ACTION_APPROVE:
			case LeaveConstants.ACTION_FINAL_APPROVE:
			case LeaveConstants.ACTION_COUNCIL_APPROVE:
				return "Your " + label + " has been APPROVED.";
			case LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW:
				return "Your " + label + " was forwarded for Administrative Review.";
			case LeaveConstants.ACTION_ADMIN_ENDORSE:
				return "Your " + label + " passed Administrative Review and was forwarded for Council Review.";
			case LeaveConstants.ACTION_COUNCIL_ENDORSE:
				return "Your " + label + " passed Council Review and awaits final approval.";
			case LeaveConstants.ACTION_DISAPPROVE:
				return "Your " + label + " has been DISAPPROVED" + reasonSuffix(remarks);
			case LeaveConstants.ACTION_REOPEN:
				return "Your " + label + " approval was reopened for review.";
			default:
				return "Your " + label + " is now " + toStatus + ".";
		}
	}

	private static String reasonSuffix(String remarks) {
		return isBlank(remarks) ? "." : ": " + remarks.trim();
	}

	private static String leaveLabel(LeaveApplication app) {
		String dates = app.getInclusiveDatesDisplay();
		return "leave application (" + nvl(app.getLeaveType())
				+ (isBlank(dates) ? "" : ", " + dates) + ")";
	}

	private static String myLeavesLink(Employee employee) {
		return "/my-leaves/" + employee.getId() + "/" + employee.getEmpHashCode();
	}

	static boolean hasDocs(LeaveApplication app) {
		return app.getSupportingDocUrls() != null && !app.getSupportingDocUrls().isEmpty();
	}

	static boolean needsDocs(LeaveApplication app) {
		return workingDays(app) > LeaveConstants.SHORT_PATH_MAX_DAYS
				&& LeaveConstants.PENDING_STATUSES.contains(nvl(app.getStatus()));
	}

	private List<Map<String, String>> docList(LeaveApplication app) {
		List<Map<String, String>> docs = new ArrayList<>();
		if (app.getSupportingDocUrls() != null) {
			for (String url : app.getSupportingDocUrls()) {
				Map<String, String> d = new LinkedHashMap<>();
				d.put("url", url);
				d.put("name", fileNameOf(url));
				docs.add(d);
			}
		}
		return docs;
	}

	private static String fileNameOf(String url) {
		if (url == null) {
			return "";
		}
		int slash = url.lastIndexOf('/');
		return slash >= 0 ? url.substring(slash + 1) : url;
	}

	private static double workingDays(LeaveApplication app) {
		return app.getWorkingDays() == null ? 0 : app.getWorkingDays();
	}

	/** The actor's position title, used to prefill their signatory title. */
	private static String actorTitle(Employee actor) {
		return actor != null && actor.getPositionTitle() != null
				? nvl(actor.getPositionTitle().getPositionTitleName()) : "";
	}

	private static String buildCardName(Employee employee) {
		StringBuilder sb = new StringBuilder();
		sb.append(nvl(employee.getLastName()).toUpperCase());
		if (employee.getFirstName() != null && !employee.getFirstName().isBlank()) {
			sb.append(", ").append(employee.getFirstName().toUpperCase());
		}
		if (employee.getMiddleName() != null && !employee.getMiddleName().isBlank()) {
			sb.append(" ").append(employee.getMiddleName().substring(0, 1).toUpperCase()).append(".");
		}
		return sb.toString();
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private static String nvl(String value) {
		return value == null ? "" : value;
	}
}
