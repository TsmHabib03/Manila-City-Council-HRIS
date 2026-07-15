package com.ian.web.employee.leave;

import java.io.InputStream;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.systemsettings.leave_type.LeaveType;
import com.ian.web.systemsettings.leave_type.LeaveTypeRepository;

import lombok.RequiredArgsConstructor;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRMapCollectionDataSource;

@Controller
@RequiredArgsConstructor
public class LeaveController {

	private static final DecimalFormat CREDIT_FMT = new DecimalFormat("0.###");

	private final EmployeeRepository employeeRepository;
	private final LeaveApplicationRepository leaveApplicationRepository;
	private final LeaveCardEntryRepository leaveCardEntryRepository;
	private final LeaveTypeRepository leaveTypeRepository;

	// ── pages ────────────────────────────────────────────────────────────────

	@GetMapping("/leaves")
	public String viewLeaveEmployeeList(Model model) {
		model.addAttribute("employeeList", employeeRepository.findAll());
		return "employee/leave/employee-list-leave";
	}

	@GetMapping("/leaves/{employeeId}/{empHashCode}")
	public String viewEmployeeLeaves(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		populateLeaveModel(model, employeeId, empHashCode, "HRADMIN");
		return "employee/leave/employee-leave-record";
	}

	@GetMapping("/my-leaves/{employeeId}/{empHashCode}")
	public String viewMyLeaves(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		populateLeaveModel(model, employeeId, empHashCode, "EMPLOYEE");
		return "employee/leave/employee-leave-record";
	}

	// ── leave application CRUD ───────────────────────────────────────────────

	@PostMapping({ "/addLeaveApplication", "/updateLeaveApplication" })
	@Transactional
	public String saveLeaveApplication(LeaveApplication application, HttpServletRequest request,
			final RedirectAttributes redirect) {

		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isStaff = actorObj != null
				&& ("ROLE_ADMIN".equals(actorObj.getUserType()) || "ROLE_HR".equals(actorObj.getUserType()));

		if (!isStaff) {
			// Employees may only file a brand-new application for themselves. Filing and
			// approving share this endpoint, so without this guard an employee could edit
			// or self-approve an existing application (the exact IDOR 0ca9c03 closed off).
			if (actorObj == null || application.getId() != 0) {
				redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
				return actorObj == null ? "redirect:/login"
						: "redirect:/my-leaves/" + actorObj.getId() + "/" + actorObj.getEmpHashCode();
			}
			application.setEmployee(actorObj);
			application.setStatus(LeaveConstants.STATUS_FILED);
			application.setRecommendation(null);
			application.setRecommendationReason(null);
			application.setApprovedDaysWithPay(null);
			application.setApprovedDaysWithoutPay(null);
			application.setApprovedOthers(null);
			application.setDisapprovalReason(null);
			application.setDisapprovedExigency(false);
			application.setCertOfficerName(LeaveConstants.DEFAULT_CERT_OFFICER_NAME);
			application.setCertOfficerTitle(LeaveConstants.DEFAULT_CERT_OFFICER_TITLE);
			application.setRecommenderName(LeaveConstants.DEFAULT_RECOMMENDER_NAME);
			application.setRecommenderTitle(LeaveConstants.DEFAULT_RECOMMENDER_TITLE);
			application.setApproverName(LeaveConstants.DEFAULT_APPROVER_NAME);
			application.setApproverTitle(LeaveConstants.DEFAULT_APPROVER_TITLE);
		}

		fillCertification(application);
		LeaveApplication saved = leaveApplicationRepository.save(application);
		syncLedgerEntry(saved);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Saved."));
		return isStaff
				? "redirect:/leaves/" + saved.getEmployee().getId() + "/" + saved.getEmployee().getEmpHashCode()
				: "redirect:/my-leaves/" + saved.getEmployee().getId() + "/" + saved.getEmployee().getEmpHashCode();
	}

	@GetMapping("/deleteLeaveApplication/{id}")
	@Transactional
	public String deleteLeaveApplication(@PathVariable long id, final RedirectAttributes redirect) {
		LeaveApplication application = leaveApplicationRepository.findById(id).orElseThrow();
		long employeeId = application.getEmployee().getId();
		String hashCode = application.getEmployee().getEmpHashCode();
		leaveCardEntryRepository.deleteAll(leaveCardEntryRepository.findByLeaveApplicationId(id));
		leaveApplicationRepository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Deleted."));
		return "redirect:/leaves/" + employeeId + "/" + hashCode;
	}

	// ── leave card entry CRUD ────────────────────────────────────────────────

	@PostMapping({ "/addLeaveCardEntry", "/updateLeaveCardEntry" })
	public String saveLeaveCardEntry(LeaveCardEntry entry, final RedirectAttributes redirect) {
		LeaveCardEntry saved = leaveCardEntryRepository.save(entry);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Leave Card Entry Successfully Saved."));
		return "redirect:/leaves/" + saved.getEmployee().getId()
				+ "/" + saved.getEmployee().getEmpHashCode();
	}

	@GetMapping("/deleteLeaveCardEntry/{id}")
	public String deleteLeaveCardEntry(@PathVariable long id, final RedirectAttributes redirect) {
		LeaveCardEntry entry = leaveCardEntryRepository.findById(id).orElseThrow();
		long employeeId = entry.getEmployee().getId();
		String hashCode = entry.getEmployee().getEmpHashCode();
		if (entry.getLeaveApplication() != null) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR",
					"This entry is linked to a leave application. Delete or un-approve the application instead."));
			return "redirect:/leaves/" + employeeId + "/" + hashCode;
		}
		leaveCardEntryRepository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Leave Card Entry Successfully Deleted."));
		return "redirect:/leaves/" + employeeId + "/" + hashCode;
	}

	/** Posts the 1.25 VL / 1.25 SL monthly accrual row for the given month. */
	@PostMapping("/postLeaveAccrual/{employeeId}/{empHashCode}")
	public String postMonthlyAccrual(@PathVariable long employeeId, @PathVariable String empHashCode,
			@RequestParam("accrualMonth") String accrualMonth, final RedirectAttributes redirect) {

		Employee employee = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode).orElseThrow();
		YearMonth month = YearMonth.parse(accrualMonth); // yyyy-MM from <input type="month">
		String period = month.atDay(1).format(LeaveConstants.CARD_PERIOD);

		if (leaveCardEntryRepository.existsByEmployeeIdAndEntryTypeAndPeriod(
				employeeId, LeaveConstants.ENTRY_ACCRUAL, period)) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR",
					"Accrual for " + period + " has already been posted."));
			return "redirect:/leaves/" + employeeId + "/" + empHashCode;
		}

		LeaveCardEntry entry = new LeaveCardEntry();
		entry.setEmployee(employee);
		entry.setEntryType(LeaveConstants.ENTRY_ACCRUAL);
		entry.setEntryDate(month.atEndOfMonth());
		entry.setPeriod(period);
		entry.setVlEarned(LeaveConstants.MONTHLY_VL_ACCRUAL);
		entry.setSlEarned(LeaveConstants.MONTHLY_SL_ACCRUAL);
		leaveCardEntryRepository.save(entry);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS",
				"Monthly accrual for " + period + " posted."));
		return "redirect:/leaves/" + employeeId + "/" + empHashCode;
	}

	// ── PDF exports ──────────────────────────────────────────────────────────

	@GetMapping("/leaveForm6Pdf/{applicationId}")
	public void exportForm6Pdf(@PathVariable long applicationId, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		LeaveApplication app = leaveApplicationRepository.findById(applicationId).orElseThrow();
		Employee employee = app.getEmployee();

		// /leaveForm6Pdf takes only a numeric id (no hash token), so guard against IDOR:
		// only staff or the application's owner may render it. Mirrors the actorObj
		// isAdmin/isOwnRecord check used in the PDS controllers.
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isStaff = actorObj != null
				&& ("ROLE_ADMIN".equals(actorObj.getUserType()) || "ROLE_HR".equals(actorObj.getUserType()));
		boolean isOwnRecord = actorObj != null && employee != null && actorObj.getId() == employee.getId();
		if (!isStaff && !isOwnRecord) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
			return;
		}

		Map<String, Object> map = new HashMap<>();
		map.put("OFFICE_DEPARTMENT", nvl(app.getOfficeDepartment()));
		map.put("LAST_NAME", nvl(employee.getLastName()).toUpperCase());
		map.put("FIRST_NAME", nvl(employee.getFirstName()).toUpperCase());
		map.put("MIDDLE_NAME", nvl(employee.getMiddleName()).toUpperCase());
		map.put("DATE_OF_FILING", formatDate(app.getDateOfFiling()));
		map.put("POSITION", nvl(app.getPosition()));
		map.put("SALARY", nvl(app.getSalary()));

		map.put("LEAVE_TYPE", nvl(app.getLeaveType()));
		map.put("LEAVE_TYPE_OTHERS", nvl(app.getLeaveTypeOthers()));
		map.put("DETAIL_OPTION", nvl(app.getLeaveDetailOption()));
		map.put("DETAIL_TEXT", nvl(app.getLeaveDetailText()));

		map.put("WORKING_DAYS", fmt(app.getWorkingDays()));
		map.put("INCLUSIVE_DATES", app.getInclusiveDatesDisplay());
		map.put("COMMUTATION", nvl(app.getCommutation()));

		map.put("CERT_AS_OF", formatDate(app.getCertAsOfDate()));
		map.put("CERT_VL_EARNED", fmt(app.getCertVlTotalEarned()));
		map.put("CERT_VL_LESS", fmt(app.getCertVlLessApplication()));
		map.put("CERT_VL_BALANCE", fmt(app.getCertVlBalance()));
		map.put("CERT_SL_EARNED", fmt(app.getCertSlTotalEarned()));
		map.put("CERT_SL_LESS", fmt(app.getCertSlLessApplication()));
		map.put("CERT_SL_BALANCE", fmt(app.getCertSlBalance()));

		map.put("RECOMMENDATION", nvl(app.getRecommendation()));
		map.put("RECOMMENDATION_REASON", nvl(app.getRecommendationReason()));
		map.put("APPROVED_DAYS_WITH_PAY", fmt(app.getApprovedDaysWithPay()));
		map.put("APPROVED_DAYS_WITHOUT_PAY", fmt(app.getApprovedDaysWithoutPay()));
		map.put("APPROVED_OTHERS", nvl(app.getApprovedOthers()));
		map.put("DISAPPROVAL_REASON", nvl(app.getDisapprovalReason()));

		map.put("CERT_OFFICER_NAME", nvl(app.getCertOfficerName()));
		map.put("CERT_OFFICER_TITLE", nvl(app.getCertOfficerTitle()));
		map.put("RECOMMENDER_NAME", nvl(app.getRecommenderName()));
		map.put("RECOMMENDER_TITLE", nvl(app.getRecommenderTitle()));
		map.put("APPROVER_NAME", nvl(app.getApproverName()));
		map.put("APPROVER_TITLE", nvl(app.getApproverTitle()));

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "inline; filename=CS-Form-6-" + applicationId + ".pdf");
		InputStream reportStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("jasper/reports/CS-Form-6-Leave.jasper");
		JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map, new JREmptyDataSource());
	}

	@GetMapping("/leaveCardPdf/{employeeId}/{empHashCode}")
	public void exportLeaveCardPdf(@PathVariable long employeeId, @PathVariable String empHashCode,
			HttpServletResponse response) throws Exception {

		Employee employee = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode).orElseThrow();
		List<LeaveCardEntry> entries = leaveCardEntryRepository.findByEmployeeIdOrderByEntryDateAscIdAsc(employeeId);
		computeRunningBalances(entries);

		List<Map<String, ?>> rows = new ArrayList<>();
		for (LeaveCardEntry e : entries) {
			Map<String, String> row = new HashMap<>();
			row.put("period", nvl(e.getPeriod()));
			row.put("particulars", nvl(e.getParticulars()));
			row.put("vlEarned", fmt(e.getVlEarned()));
			row.put("vlDeducted", fmt(e.getVlDeducted()));
			row.put("vlBalance", fmt(e.getVlBalance()));
			row.put("slEarned", fmt(e.getSlEarned()));
			row.put("slDeducted", fmt(e.getSlDeducted()));
			row.put("slBalance", fmt(e.getSlBalance()));
			row.put("remarks", nvl(e.getRemarks()));
			rows.add(row);
		}
		if (rows.isEmpty()) {
			rows.add(new HashMap<String, String>());
		}

		Map<String, Object> map = new HashMap<>();
		map.put("EMP_NAME", buildCardName(employee));
		map.put("FIRST_DAY_OF_SERVICE", formatDate(employee.getAssumptiondate()));
		map.put("PREPARED_BY_NAME", LeaveConstants.DEFAULT_CARD_PREPARED_BY_NAME);
		map.put("PREPARED_BY_TITLE", LeaveConstants.DEFAULT_CARD_PREPARED_BY_TITLE);
		map.put("CERTIFIED_BY_NAME", LeaveConstants.DEFAULT_CARD_CERTIFIED_BY_NAME);
		map.put("CERTIFIED_BY_TITLE", LeaveConstants.DEFAULT_CARD_CERTIFIED_BY_TITLE);

		response.setContentType("application/pdf");
		response.setHeader("Content-Disposition", "inline; filename=Leave-Card-" + employeeId + ".pdf");
		InputStream reportStream = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream("jasper/reports/Leave-Card.jasper");
		JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map,
				new JRMapCollectionDataSource(rows));
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private void populateLeaveModel(Model model, long employeeId, String empHashCode, String showMode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		if (!optional.isPresent()) {
			model.addAttribute("msg", new UXMessage("EMP-NOT-FOUND",
					"Employee Not Found. You will be redirected to the dashboard."));
		}

		Employee employee = optional.orElseGet(Employee::new);
		model.addAttribute("employee", employee);
		model.addAttribute("showMode", showMode);

		LeaveApplication application = new LeaveApplication();
		application.setEmployee(employee);
		application.setDateOfFiling(LocalDate.now());
		application.setOfficeDepartment(employee.getDivision() != null
				? employee.getDivision().getDivisionName() : "City Council of Manila");
		application.setPosition(employee.getPositionTitle() != null
				? employee.getPositionTitle().getPositionTitleName() : "");
		model.addAttribute("leaveApplication", application);

		LeaveCardEntry cardEntry = new LeaveCardEntry();
		cardEntry.setEmployee(employee);
		model.addAttribute("leaveCardEntry", cardEntry);

		List<String> leaveTypes = new ArrayList<>();
		for (LeaveType type : leaveTypeRepository.findByIsActiveTrueOrderByIdAsc()) {
			leaveTypes.add(type.getLeaveTypeName());
		}
		model.addAttribute("leaveTypeList", leaveTypes.isEmpty() ? LeaveConstants.LEAVE_TYPE_LIST : leaveTypes);
		model.addAttribute("leaveDetailOptionList", LeaveConstants.LEAVE_DETAIL_OPTION_LIST);
		model.addAttribute("statusList", LeaveConstants.STATUS_LIST);
		model.addAttribute("entryTypeList", LeaveConstants.ENTRY_TYPE_LIST);

		model.addAttribute("leaveApplicationList",
				leaveApplicationRepository.findByEmployeeIdOrderByDateOfFilingDesc(employeeId));

		List<LeaveCardEntry> cardEntries = leaveCardEntryRepository.findByEmployeeIdOrderByEntryDateAscIdAsc(employeeId);
		double[] totals = computeRunningBalances(cardEntries);
		model.addAttribute("leaveCardEntryList", cardEntries);
		model.addAttribute("vlBalance", CREDIT_FMT.format(totals[0]));
		model.addAttribute("slBalance", CREDIT_FMT.format(totals[1]));
	}

	/** Sets the running VL/SL balances on each entry; returns {vlTotal, slTotal}. */
	private double[] computeRunningBalances(List<LeaveCardEntry> entries) {
		double vl = 0;
		double sl = 0;
		for (LeaveCardEntry e : entries) {
			vl += e.vl();
			sl += e.sl();
			e.setVlBalance(round3(vl));
			e.setSlBalance(round3(sl));
		}
		return new double[] { round3(vl), round3(sl) };
	}

	/**
	 * Auto-fills the 7.A certification block from the current ledger balances,
	 * excluding any entry already posted by this same application.
	 */
	private void fillCertification(LeaveApplication application) {
		if (application.getEmployee() == null) {
			return;
		}
		List<LeaveCardEntry> entries = leaveCardEntryRepository
				.findByEmployeeIdOrderByEntryDateAscIdAsc(application.getEmployee().getId());
		double vl = 0;
		double sl = 0;
		for (LeaveCardEntry e : entries) {
			if (e.getLeaveApplication() != null && e.getLeaveApplication().getId() == application.getId()) {
				continue;
			}
			vl += e.vl();
			sl += e.sl();
		}

		double days = application.getWorkingDays() == null ? 0 : application.getWorkingDays();
		boolean deductsVl = LeaveConstants.DEDUCTS_VL.contains(application.getLeaveType());
		boolean deductsSl = LeaveConstants.DEDUCTS_SL.contains(application.getLeaveType());

		if (application.getCertAsOfDate() == null) {
			application.setCertAsOfDate(application.getDateOfFiling() != null
					? application.getDateOfFiling() : LocalDate.now());
		}
		application.setCertVlTotalEarned(round3(vl));
		application.setCertVlLessApplication(deductsVl ? days : 0d);
		application.setCertVlBalance(round3(vl - (deductsVl ? days : 0)));
		application.setCertSlTotalEarned(round3(sl));
		application.setCertSlLessApplication(deductsSl ? days : 0d);
		application.setCertSlBalance(round3(sl - (deductsSl ? days : 0)));
	}

	/**
	 * Keeps the leave card in sync with the application: removes any entry the
	 * application posted before, then re-posts one if the application is
	 * APPROVED. Non-deducting leave types are recorded with no deduction, per
	 * the ledger behavior on the sample card.
	 */
	private void syncLedgerEntry(LeaveApplication application) {
		leaveCardEntryRepository.deleteAll(leaveCardEntryRepository.findByLeaveApplicationId(application.getId()));

		if (!LeaveConstants.STATUS_APPROVED.equals(application.getStatus())) {
			return;
		}

		double days = application.getWorkingDays() == null ? 0 : application.getWorkingDays();
		boolean deductsVl = LeaveConstants.DEDUCTS_VL.contains(application.getLeaveType());
		boolean deductsSl = LeaveConstants.DEDUCTS_SL.contains(application.getLeaveType());

		LeaveCardEntry entry = new LeaveCardEntry();
		entry.setEmployee(application.getEmployee());
		entry.setLeaveApplication(application);
		entry.setEntryType(LeaveConstants.ENTRY_LEAVE);
		entry.setEntryDate(application.getDateFrom() != null ? application.getDateFrom()
				: (application.getDateOfFiling() != null ? application.getDateOfFiling() : LocalDate.now()));
		entry.setParticulars("(" + CREDIT_FMT.format(days) + "-0-0) "
				+ LeaveConstants.particularsCode(application.getLeaveType()));
		entry.setVlDeducted(deductsVl ? days : null);
		entry.setSlDeducted(deductsSl ? days : null);
		entry.setRemarks(application.getInclusiveDatesDisplay());
		leaveCardEntryRepository.save(entry);
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

	private static double round3(double value) {
		return Math.round(value * 1000d) / 1000d;
	}

	private static String fmt(Double value) {
		return value == null ? "" : CREDIT_FMT.format(value);
	}

	private static String nvl(String value) {
		return value == null ? "" : value;
	}

	private static String formatDate(LocalDate date) {
		return date == null ? "" : date.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
	}
}
