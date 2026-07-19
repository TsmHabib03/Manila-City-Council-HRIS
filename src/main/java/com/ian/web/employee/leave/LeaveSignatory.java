package com.ian.web.employee.leave;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Single-row settings record holding the default signatories printed on new
 * leave applications (CS Form 6), editable from System Settings so future
 * officials can be set without a code change. The endorser is the Leave
 * Endorsement signatory requested by CR Request ID 015 (2026-07-16).
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class LeaveSignatory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String certOfficerName = LeaveConstants.DEFAULT_CERT_OFFICER_NAME;
	private String certOfficerTitle = LeaveConstants.DEFAULT_CERT_OFFICER_TITLE;
	private String recommenderName = LeaveConstants.DEFAULT_RECOMMENDER_NAME;
	private String recommenderTitle = LeaveConstants.DEFAULT_RECOMMENDER_TITLE;
	private String approverName = LeaveConstants.DEFAULT_APPROVER_NAME;
	private String approverTitle = LeaveConstants.DEFAULT_APPROVER_TITLE;
	private String endorserName;
	private String endorserTitle;
}
