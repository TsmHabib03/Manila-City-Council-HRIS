package com.ian.web.config.security;

import com.ian.web.employee.Employee;

/**
 * Login roles (Employee.userType values). CR Request ID 016 v2 adds dedicated
 * accounts for the leave-workflow actors: Supervisor, Secretary to the City
 * Council and Vice-Mayor, each acting only at their own stage. ADMIN/HR keep
 * the ability to record any stage on behalf of (acting) signatories.
 */
public final class Roles {

	private Roles() {
	}

	public static final String ADMIN = "ROLE_ADMIN";
	public static final String HR = "ROLE_HR";
	public static final String EMPLOYEE = "ROLE_EMPLOYEE";

	// CR Request ID 016 v2: workflow actor accounts
	public static final String SUPERVISOR = "ROLE_SUPERVISOR";
	public static final String COUNCIL = "ROLE_COUNCIL";
	public static final String VICEMAYOR = "ROLE_VICEMAYOR";

	/** Every role allowed to act on the leave workflow (staff + stage actors). */
	public static final String[] WORKFLOW_ROLES = {ADMIN, HR, SUPERVISOR, COUNCIL, VICEMAYOR};

	/** HR staff: may record every workflow stage and manage leave records. */
	public static boolean isStaff(Employee employee) {
		return hasRole(employee, ADMIN) || hasRole(employee, HR);
	}

	/** Any account that participates in the leave decision flow. */
	public static boolean isWorkflowActor(Employee employee) {
		return isStaff(employee)
				|| hasRole(employee, SUPERVISOR)
				|| hasRole(employee, COUNCIL)
				|| hasRole(employee, VICEMAYOR);
	}

	public static boolean hasRole(Employee employee, String role) {
		return employee != null && role.equals(employee.getUserType());
	}
}
