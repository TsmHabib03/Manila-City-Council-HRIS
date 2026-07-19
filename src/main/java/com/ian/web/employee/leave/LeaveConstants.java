package com.ian.web.employee.leave;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;

/**
 * Leave-management constants: CS Form 6 leave types, deduction rules and the
 * default signatories printed on the Manila City Council version of the form.
 * Signatory defaults are editable per application so future officials can be
 * set without a code change.
 */
public final class LeaveConstants {

	private LeaveConstants() {
	}

	// Application statuses (workflow per PM-FAD-02 leave processing manual)
	public static final String STATUS_FILED = "FILED";
	public static final String STATUS_RETURNED = "RETURNED";
	public static final String STATUS_APPROVED = "APPROVED";
	public static final String STATUS_DISAPPROVED = "DISAPPROVED";
	public static final String STATUS_CANCELLED = "CANCELLED";

	// CR Request ID 016: multi-stage leave decision flow statuses
	public static final String STATUS_ENDORSED = "ENDORSED";
	public static final String STATUS_FOR_ADMIN_REVIEW = "FOR_ADMIN_REVIEW";
	public static final String STATUS_FOR_COUNCIL_REVIEW = "FOR_COUNCIL_REVIEW";
	public static final String STATUS_FOR_FINAL_APPROVAL = "FOR_FINAL_APPROVAL";
	public static final String STATUS_APPEALED = "APPEALED";
	/** CR 016 v2: HR screening done, awaiting the Supervisor account's endorsement. */
	public static final String STATUS_FOR_ENDORSEMENT = "FOR_ENDORSEMENT";

	public static final List<String> STATUS_LIST = List.of(
			STATUS_FILED, STATUS_RETURNED, STATUS_APPROVED, STATUS_DISAPPROVED, STATUS_CANCELLED);

	/** Statuses still moving through the decision flow (not yet final). */
	public static final List<String> PENDING_STATUSES = List.of(
			STATUS_FILED, STATUS_APPEALED, STATUS_RETURNED, STATUS_FOR_ENDORSEMENT, STATUS_ENDORSED,
			STATUS_FOR_ADMIN_REVIEW, STATUS_FOR_COUNCIL_REVIEW, STATUS_FOR_FINAL_APPROVAL);

	// CR Request ID 016: decision-flow workflow actions (recorded per transition)
	public static final String ACTION_RETURN = "RETURN";
	/** CR 016 v2: HR screening passed — hands the request to the Supervisor's queue. */
	public static final String ACTION_FORWARD_TO_SUPERVISOR = "FORWARD_TO_SUPERVISOR";
	public static final String ACTION_ENDORSE = "ENDORSE";
	public static final String ACTION_APPROVE = "APPROVE";
	public static final String ACTION_SEND_TO_ADMIN_REVIEW = "SEND_TO_ADMIN_REVIEW";
	public static final String ACTION_ADMIN_ENDORSE = "ADMIN_ENDORSE";
	public static final String ACTION_COUNCIL_ENDORSE = "COUNCIL_ENDORSE";
	public static final String ACTION_FINAL_APPROVE = "FINAL_APPROVE";
	public static final String ACTION_DISAPPROVE = "DISAPPROVE";
	public static final String ACTION_CANCEL = "CANCEL";
	public static final String ACTION_APPEAL = "APPEAL";
	public static final String ACTION_REOPEN = "REOPEN";
	/** Non-transition audit marker: verification receipt printed. */
	public static final String ACTION_RECEIPT_PRINTED = "RECEIPT_PRINTED";

	/** Leaves of at most this many working days may be approved directly by HR. */
	public static final double SHORT_PATH_MAX_DAYS = 5;
	/** Leaves longer than this many working days additionally require Council Review. */
	public static final double COUNCIL_THRESHOLD_DAYS = 15;
	/** Mandatory/forced leave days every employee must use (or forfeit) per year. */
	public static final double MANDATORY_LEAVE_DAYS = 5;
	/** Ledger period marker for the year-end mandatory deduction, e.g. "YE-2026". */
	public static final String YEAR_END_PERIOD_PREFIX = "YE-";

	// 6.A Type of leave (CS Form No. 6, Revised 2020)
	public static final String LT_VACATION = "Vacation Leave";
	public static final String LT_FORCED = "Mandatory/Forced Leave";
	public static final String LT_SICK = "Sick Leave";
	public static final String LT_MATERNITY = "Maternity Leave";
	public static final String LT_PATERNITY = "Paternity Leave";
	public static final String LT_SPL = "Special Privilege Leave";
	public static final String LT_SOLO_PARENT = "Solo Parent Leave";
	public static final String LT_STUDY = "Study Leave";
	public static final String LT_VAWC = "10-Day VAWC Leave";
	public static final String LT_REHAB = "Rehabilitation Privilege";
	public static final String LT_WOMEN = "Special Leave Benefits for Women";
	public static final String LT_CALAMITY = "Special Emergency (Calamity) Leave";
	public static final String LT_ADOPTION = "Adoption Leave";
	public static final String LT_MONETIZATION = "Monetization of Leave Credits";
	public static final String LT_TERMINAL = "Terminal Leave";
	public static final String LT_OTHERS = "Others";

	public static final List<String> LEAVE_TYPE_LIST = List.of(
			LT_VACATION, LT_FORCED, LT_SICK, LT_MATERNITY, LT_PATERNITY, LT_SPL,
			LT_SOLO_PARENT, LT_STUDY, LT_VAWC, LT_REHAB, LT_WOMEN, LT_CALAMITY,
			LT_ADOPTION, LT_MONETIZATION, LT_TERMINAL, LT_OTHERS);

	// 6.B detail options (one selected sub-option + free text per application)
	public static final List<String> LEAVE_DETAIL_OPTION_LIST = List.of(
			"Within the Philippines", "Abroad",
			"In Hospital", "Out Patient",
			"Special Leave Benefits for Women - Illness",
			"Completion of Master's Degree", "BAR/Board Examination Review",
			"Other purpose");

	/**
	 * Ledger deduction rules observed on the Employee's Leave Card sample:
	 * V and Forced deduct Vacation Leave, S deducts Sick Leave; every other
	 * leave type is recorded on the card without deducting either balance.
	 */
	public static final Set<String> DEDUCTS_VL = Set.of(LT_VACATION, LT_FORCED);
	public static final Set<String> DEDUCTS_SL = Set.of(LT_SICK);

	/** Particulars code used on the card, e.g. "(1-0-0) V". */
	public static String particularsCode(String leaveType) {
		if (leaveType == null) return "";
		switch (leaveType) {
			case LT_VACATION: return "V";
			case LT_FORCED: return "F";
			case LT_SICK: return "S";
			case LT_SPL: return "SPL";
			case LT_CALAMITY: return "Calamity Leave";
			case LT_MATERNITY: return "Maternity Leave";
			case LT_PATERNITY: return "Paternity Leave";
			case LT_SOLO_PARENT: return "Solo Parent Leave";
			case LT_STUDY: return "Study Leave";
			case LT_VAWC: return "VAWC Leave";
			case LT_REHAB: return "Rehabilitation Leave";
			case LT_WOMEN: return "SLBW";
			case LT_ADOPTION: return "Adoption Leave";
			case LT_MONETIZATION: return "Monetization";
			case LT_TERMINAL: return "Terminal Leave";
			default: return leaveType;
		}
	}

	// Monthly accrual per CSC rules as shown on the sample card
	public static final double MONTHLY_VL_ACCRUAL = 1.25;
	public static final double MONTHLY_SL_ACCRUAL = 1.25;

	// Leave card entry types
	public static final String ENTRY_ACCRUAL = "ACCRUAL";
	public static final String ENTRY_LEAVE = "LEAVE";
	public static final String ENTRY_RESTORATION = "RESTORATION";
	public static final String ENTRY_ADJUSTMENT = "ADJUSTMENT";

	public static final List<String> ENTRY_TYPE_LIST = List.of(
			ENTRY_ACCRUAL, ENTRY_LEAVE, ENTRY_RESTORATION, ENTRY_ADJUSTMENT);

	// Default signatories on the Manila City Council CS Form 6
	public static final String DEFAULT_CERT_OFFICER_NAME = "GLORIA S. TIRADO";
	public static final String DEFAULT_CERT_OFFICER_TITLE = "Acting Chief Administrative Officer, concurrent HRM Section";
	public static final String DEFAULT_RECOMMENDER_NAME = "ATTY. HANS ROGER S. LUNA";
	public static final String DEFAULT_RECOMMENDER_TITLE = "City Gov't Dept. Head III (Secretary to the City Council)";
	public static final String DEFAULT_APPROVER_NAME = "ANGELA LEI \"CHI\" L. ATIENZA";
	public static final String DEFAULT_APPROVER_TITLE = "Vice Mayor and Presiding Officer";

	// Leave card footer signatories
	public static final String DEFAULT_CARD_PREPARED_BY_NAME = "CHARITO A. RUMBO";
	public static final String DEFAULT_CARD_PREPARED_BY_TITLE = "Administrative Officer IV";
	public static final String DEFAULT_CARD_CERTIFIED_BY_NAME = "EDUARDO S. DEGRACIA";
	public static final String DEFAULT_CARD_CERTIFIED_BY_TITLE = "Chief Administrative Officer";

	/** Date style used on the leave card (e.g. 12/26/19). */
	public static final DateTimeFormatter CARD_DATE = DateTimeFormatter.ofPattern("M/d/yy");
	/** Accrual period style on the leave card (e.g. 12/25). */
	public static final DateTimeFormatter CARD_PERIOD = DateTimeFormatter.ofPattern("M/yy");
}
