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
 * Stages are recorded by HR/Admin on behalf of the physical signatories; every
 * step is audit-trailed and the employee is notified in-app. The ledger
 * deduction still only ever happens at APPROVED (LeaveLedger.syncLedgerEntry).
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
		Employee actor = (Employee) request.getSession().getAttribute("actorObj");
		String back = "redirect:/leaves/" + app.getEmployee().getId() + "/" + app.getEmployee().getEmpHashCode();

		String error = validateAction(app, action, signatoryName, remarks);
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

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", actionLabel(action) + " recorded."));
		return back;
	}

	/** HR attaches the supporting documents received for a > 5-day leave. */
	@PostMapping("/leave-workflow/{id}/upload-docs")
	@Transactional
	public String uploadSupportingDocs(@PathVariable long id,
			@RequestParam("supportingDocs") MultipartFile[] files,
			final RedirectAttributes redirect) {

		// Managed entity: @ElementCollection updates are unreliable on detached merges.
		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		String back = "redirect:/leaves/" + app.getEmployee().getId() + "/" + app.getEmployee().getEmpHashCode();

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
	public Map<String, Object> workflowPanel(@PathVariable long id) {
		LeaveApplication app = leaveApplicationRepository.findById(id).orElseThrow();
		LeaveSignatory signatory = leaveSignatoryRepository.findAll().stream()
				.findFirst().orElseGet(LeaveSignatory::new);

		Map<String, Object> panel = new LinkedHashMap<>();
		panel.put("id", app.getId());
		panel.put("status", app.getStatus());
		panel.put("workingDays", app.getWorkingDays());
		panel.put("needsDocs", needsDocs(app));
		panel.put("hasDocs", hasDocs(app));
		panel.put("docs", docList(app));

		List<Map<String, Object>> actions = new ArrayList<>();
		for (String action : allowedActions(app)) {
			Map<String, Object> a = new LinkedHashMap<>();
			a.put("action", action);
			a.put("label", actionLabel(action));
			a.put("requiresSignatory", requiresSignatory(action));
			a.put("requiresRemarks", requiresRemarks(action));
			a.put("signatoryName", suggestedSignatoryName(action, signatory));
			a.put("signatoryTitle", suggestedSignatoryTitle(action, signatory));
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
		map.put("VERIFIED_BY", actor != null ? buildCardName(actor) : "");
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
				return List.of(LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_RETURN);
			case LeaveConstants.STATUS_RETURNED:
				return List.of(LeaveConstants.ACTION_ENDORSE);
			case LeaveConstants.STATUS_ENDORSED:
				return workingDays(app) <= LeaveConstants.SHORT_PATH_MAX_DAYS
						? List.of(LeaveConstants.ACTION_APPROVE, LeaveConstants.ACTION_DISAPPROVE)
						: List.of(LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_FOR_ADMIN_REVIEW:
				return List.of(LeaveConstants.ACTION_ADMIN_ENDORSE, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_FOR_COUNCIL_REVIEW:
				return List.of(LeaveConstants.ACTION_COUNCIL_ENDORSE, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_FOR_FINAL_APPROVAL:
				return List.of(LeaveConstants.ACTION_FINAL_APPROVE, LeaveConstants.ACTION_DISAPPROVE);
			case LeaveConstants.STATUS_APPROVED:
				return List.of(LeaveConstants.ACTION_REOPEN);
			default:
				return List.of();
		}
	}

	/** Returns an error message, or null when the action is valid for this application. */
	private String validateAction(LeaveApplication app, String action, String signatoryName, String remarks) {
		if (!allowedActions(app).contains(action)) {
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
			case LeaveConstants.ACTION_ENDORSE:
				return LeaveConstants.STATUS_ENDORSED;
			case LeaveConstants.ACTION_APPROVE:
			case LeaveConstants.ACTION_FINAL_APPROVE:
				return LeaveConstants.STATUS_APPROVED;
			case LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW:
				return LeaveConstants.STATUS_FOR_ADMIN_REVIEW;
			case LeaveConstants.ACTION_ADMIN_ENDORSE:
				// Council Review only for leaves of more than 15 working days.
				return workingDays(app) > LeaveConstants.COUNCIL_THRESHOLD_DAYS
						? LeaveConstants.STATUS_FOR_COUNCIL_REVIEW
						: LeaveConstants.STATUS_FOR_FINAL_APPROVAL;
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
				|| LeaveConstants.ACTION_FINAL_APPROVE.equals(action);
	}

	static boolean requiresRemarks(String action) {
		return LeaveConstants.ACTION_RETURN.equals(action)
				|| LeaveConstants.ACTION_DISAPPROVE.equals(action);
	}

	static String actionLabel(String action) {
		switch (nvl(action)) {
			case LeaveConstants.ACTION_RETURN: return "Return to Employee";
			case LeaveConstants.ACTION_ENDORSE: return "Endorse to Supervisor";
			case LeaveConstants.ACTION_APPROVE: return "Approve (HR)";
			case LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW: return "Forward for Administrative Review";
			case LeaveConstants.ACTION_ADMIN_ENDORSE: return "Administrative Review Passed";
			case LeaveConstants.ACTION_COUNCIL_ENDORSE: return "Council Review Passed";
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
			case LeaveConstants.ACTION_COUNCIL_ENDORSE: return nvl(s.getRecommenderName());
			case LeaveConstants.ACTION_FINAL_APPROVE: return nvl(s.getApproverName());
			default: return "";
		}
	}

	private static String suggestedSignatoryTitle(String action, LeaveSignatory s) {
		switch (nvl(action)) {
			case LeaveConstants.ACTION_ENDORSE: return nvl(s.getEndorserTitle());
			case LeaveConstants.ACTION_ADMIN_ENDORSE: return nvl(s.getCertOfficerTitle());
			case LeaveConstants.ACTION_COUNCIL_ENDORSE: return nvl(s.getRecommenderTitle());
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
				return "Your " + label + " was returned by HR" + reasonSuffix(remarks);
			case LeaveConstants.ACTION_ENDORSE:
				return "Your " + label + " has been endorsed to your supervisor.";
			case LeaveConstants.ACTION_APPROVE:
			case LeaveConstants.ACTION_FINAL_APPROVE:
				return "Your " + label + " has been APPROVED.";
			case LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW:
				return "Your " + label + " was forwarded for Administrative Review.";
			case LeaveConstants.ACTION_ADMIN_ENDORSE:
				return LeaveConstants.STATUS_FOR_COUNCIL_REVIEW.equals(toStatus)
						? "Your " + label + " passed Administrative Review and was forwarded for Council Review."
						: "Your " + label + " passed Administrative Review and awaits final approval.";
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
