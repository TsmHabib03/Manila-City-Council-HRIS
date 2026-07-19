package com.ian.web.employee.leave;

import java.text.DecimalFormat;
import java.time.LocalDate;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * CR Request ID 016: year-end mandatory/forced leave deduction.
 *
 * At the end of each year every employee's remaining leave balance is reduced
 * by the unused portion of the five (5) mandatory/forced leave days: an
 * employee who used 3 forced-leave days is deducted the remaining 2; one who
 * used all 5 is not deducted. Posted as an ADJUSTMENT row on the leave card
 * with period "YE-<year>", which doubles as the idempotency guard — employees
 * already processed for a year are skipped, so the run is safe to repeat and
 * safe to resume after a restart. Runs from the admin button on
 * /leave-applications and automatically every December 31 (Asia/Manila).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaveYearEndProcessor {

	private static final DecimalFormat CREDIT_FMT = new DecimalFormat("0.###");

	private final EmployeeRepository employeeRepository;
	private final LeaveApplicationRepository leaveApplicationRepository;
	private final LeaveCardEntryRepository leaveCardEntryRepository;

	/** Runs the deduction for every ACTIVE employee; returns a human-readable summary. */
	@Transactional
	public String run(int year) {
		String period = LeaveConstants.YEAR_END_PERIOD_PREFIX + year;
		int processed = 0;
		int alreadyDone = 0;
		int fullyUsed = 0;

		for (Employee employee : employeeRepository.findAll()) {
			// Active employees only ("ACTIVE" in production data, "A" in dev seeds).
			String status = employee.getStatus() == null ? "" : employee.getStatus().trim();
			if (!"ACTIVE".equalsIgnoreCase(status) && !"A".equalsIgnoreCase(status)) {
				continue;
			}
			if (leaveCardEntryRepository.existsByEmployeeIdAndEntryTypeAndPeriod(
					employee.getId(), LeaveConstants.ENTRY_ADJUSTMENT, period)) {
				alreadyDone++;
				continue;
			}

			double used = forcedLeaveDaysUsed(employee.getId(), year);
			double deduct = Math.max(0, LeaveConstants.MANDATORY_LEAVE_DAYS - used);
			if (deduct == 0) {
				fullyUsed++;
				continue;
			}

			LeaveCardEntry entry = new LeaveCardEntry();
			entry.setEmployee(employee);
			entry.setEntryType(LeaveConstants.ENTRY_ADJUSTMENT);
			entry.setEntryDate(LocalDate.of(year, 12, 31));
			entry.setPeriod(period);
			entry.setParticulars("Year-end mandatory/forced leave deduction");
			entry.setVlDeducted(deduct);
			entry.setRemarks("Forced leave used: " + CREDIT_FMT.format(used)
					+ " of " + CREDIT_FMT.format(LeaveConstants.MANDATORY_LEAVE_DAYS)
					+ "; unused " + CREDIT_FMT.format(deduct) + " day(s) deducted");
			leaveCardEntryRepository.save(entry);
			processed++;
		}

		String summary = "Year-end " + year + " mandatory leave deduction: "
				+ processed + " employee(s) deducted, "
				+ alreadyDone + " already processed, "
				+ fullyUsed + " with all " + (int) LeaveConstants.MANDATORY_LEAVE_DAYS
				+ " days used (no deduction).";
		log.info(summary);
		return summary;
	}

	/** Approved Mandatory/Forced Leave working days with a start date in the given year. */
	private double forcedLeaveDaysUsed(long employeeId, int year) {
		double used = 0;
		for (LeaveApplication app : leaveApplicationRepository
				.findByEmployeeIdAndLeaveTypeAndStatusOrderByDateFromAsc(
						employeeId, LeaveConstants.LT_FORCED, LeaveConstants.STATUS_APPROVED)) {
			if (app.getDateFrom() != null && app.getDateFrom().getYear() == year
					&& app.getWorkingDays() != null) {
				used += app.getWorkingDays();
			}
		}
		return used;
	}

	/** Automatic run every December 31 at 23:30 Manila time for the closing year. */
	@Scheduled(cron = "0 30 23 31 12 ?", zone = "Asia/Manila")
	public void runScheduled() {
		int year = LocalDate.now().getYear();
		log.info("Scheduled year-end mandatory leave deduction starting for {}", year);
		run(year);
	}
}
