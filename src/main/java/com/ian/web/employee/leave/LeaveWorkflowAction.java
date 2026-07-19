package com.ian.web.employee.leave;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Audit-trail row for every step a leave application takes through the
 * multi-stage decision flow (CR Request ID 016). Stages are recorded by
 * HR/Admin on behalf of the physical signatories, so both the acting HRIS
 * user and the signatory whose decision is being recorded are kept.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LeaveWorkflowAction {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "leave_application_id")
	private LeaveApplication leaveApplication;

	/** Status before the action; null for non-transition actions (e.g. receipt print). */
	private String fromStatus;

	/** Status after the action; null for non-transition actions. */
	private String toStatus;

	/** One of the LeaveConstants.ACTION_* names. */
	private String action;

	/** HRIS login that performed the action. */
	private String actorUsername;

	/** Display name of the HRIS user that performed the action. */
	private String actorName;

	/** Physical signatory whose decision is being recorded (endorser, CAO, VM...). */
	private String signatoryName;
	private String signatoryTitle;

	private String remarks;

	private LocalDateTime createdAt;
}
