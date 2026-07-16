package com.ian.web.employee.leave;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * One ledger row of the Employee's Leave Card: monthly accrual, leave usage,
 * restoration or manual adjustment. Balances are running totals computed at
 * render/export time, never stored.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LeaveCardEntry {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;

	/** ACCRUAL, LEAVE, RESTORATION or ADJUSTMENT. */
	private String entryType;

	/** Used only to order the ledger chronologically. */
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate entryDate;

	/** Card PERIOD column, e.g. "12/25" on accrual rows; blank otherwise. */
	private String period;

	/** Card PARTICULARS column, e.g. "(1-0-0) V", "Restoration". */
	private String particulars;

	private Double vlEarned;
	private Double vlDeducted;
	private Double slEarned;
	private Double slDeducted;

	/** Absence/UT WITHOUT pay — informational columns; leave-without-pay
	 *  consumes no earned credits, so these never affect vl()/sl(). */
	private Double vlDeductedNoPay;
	private Double slDeductedNoPay;

	/** Card REMARKS column: actual dates of leave, e.g. "12/26/19, 1/2/20". */
	private String remarks;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "leave_application_id")
	private LeaveApplication leaveApplication;

	// Running balances computed by LeaveLedger when listing/exporting
	@Transient
	private Double vlBalance;
	@Transient
	private Double slBalance;

	public double vl() {
		return (vlEarned == null ? 0 : vlEarned) - (vlDeducted == null ? 0 : vlDeducted);
	}

	public double sl() {
		return (slEarned == null ? 0 : slEarned) - (slDeducted == null ? 0 : slDeducted);
	}
}
