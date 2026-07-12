package com.ian.web.employee.leave;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One row per CS Form No. 6 (Revised 2020) Application for Leave.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LeaveApplication {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;

	// ── 1-5: header (snapshots at time of filing) ────────────────────────────
	private String officeDepartment;
	private String position;
	private String salary;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfFiling;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfReceipt;

	// ── 6.A / 6.B: type of leave and details ────────────────────────────────
	private String leaveType;
	private String leaveTypeOthers;
	private String leaveDetailOption;
	private String leaveDetailText;

	// ── 6.C: working days and inclusive dates ───────────────────────────────
	private Double workingDays;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;

	/** Free-text dates as they should appear on the card, e.g. "12/26/19, 1/2/20". */
	private String inclusiveDates;

	// ── 6.D: commutation ─────────────────────────────────────────────────────
	private String commutation;

	// ── 7.A: certification of leave credits ─────────────────────────────────
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate certAsOfDate;
	private Double certVlTotalEarned;
	private Double certVlLessApplication;
	private Double certVlBalance;
	private Double certSlTotalEarned;
	private Double certSlLessApplication;
	private Double certSlBalance;

	// ── 7.B: recommendation ──────────────────────────────────────────────────
	private String recommendation;
	private String recommendationReason;

	// ── 7.C / 7.D: action ────────────────────────────────────────────────────
	private String status = LeaveConstants.STATUS_FILED;
	private Double approvedDaysWithPay;
	private Double approvedDaysWithoutPay;
	private String approvedOthers;
	private String disapprovalReason;
	/** Disapproved due to exigency of the service: no deduction of leave credits. */
	private boolean disapprovedExigency;

	// ── Printed signatories (prefilled with current officials, editable) ────
	private String certOfficerName = LeaveConstants.DEFAULT_CERT_OFFICER_NAME;
	private String certOfficerTitle = LeaveConstants.DEFAULT_CERT_OFFICER_TITLE;
	private String recommenderName = LeaveConstants.DEFAULT_RECOMMENDER_NAME;
	private String recommenderTitle = LeaveConstants.DEFAULT_RECOMMENDER_TITLE;
	private String approverName = LeaveConstants.DEFAULT_APPROVER_NAME;
	private String approverTitle = LeaveConstants.DEFAULT_APPROVER_TITLE;

	private String remarks;

	public String getInclusiveDatesDisplay() {
		if (inclusiveDates != null && !inclusiveDates.isBlank()) {
			return inclusiveDates;
		}
		if (dateFrom == null) {
			return "";
		}
		if (dateTo == null || dateTo.equals(dateFrom)) {
			return LeaveConstants.CARD_DATE.format(dateFrom);
		}
		return LeaveConstants.CARD_DATE.format(dateFrom) + " - " + LeaveConstants.CARD_DATE.format(dateTo);
	}
}
