package com.ian.web.employee.leave;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * Leave-card ledger rules shared by the CRUD controller and the CR-016
 * decision-flow controller: running balances, the 7.A certification snapshot
 * and the application-to-ledger sync. Extracted unchanged from
 * LeaveController so approval from either endpoint posts identical entries.
 */
@Component
@RequiredArgsConstructor
public class LeaveLedger {

	private static final DecimalFormat CREDIT_FMT = new DecimalFormat("0.###");

	private final LeaveCardEntryRepository leaveCardEntryRepository;

	/** Sets the running VL/SL balances on each entry; returns {vlTotal, slTotal}. */
	public double[] computeRunningBalances(List<LeaveCardEntry> entries) {
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
	public void fillCertification(LeaveApplication application) {
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
	public void syncLedgerEntry(LeaveApplication application) {
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
		Double daysNoPay = application.getApprovedDaysWithoutPay();
		if (daysNoPay != null && daysNoPay > 0) {
			entry.setVlDeductedNoPay(deductsVl ? daysNoPay : null);
			entry.setSlDeductedNoPay(deductsSl ? daysNoPay : null);
		}
		entry.setRemarks(application.getInclusiveDatesDisplay());
		leaveCardEntryRepository.save(entry);
	}

	static double round3(double value) {
		return Math.round(value * 1000d) / 1000d;
	}
}
