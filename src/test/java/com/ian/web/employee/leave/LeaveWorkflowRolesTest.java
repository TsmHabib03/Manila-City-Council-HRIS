package com.ian.web.employee.leave;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.ian.web.config.security.Roles;
import com.ian.web.employee.Employee;

/**
 * CR 016 v2: per-role workflow authorization matrix and the new HR-screening →
 * Supervisor-endorsement transition. Pure unit tests — no Spring context.
 */
class LeaveWorkflowRolesTest {

	private final LeaveWorkflowController controller =
			new LeaveWorkflowController(null, null, null, null, null, null);

	private static Employee actor(String role) {
		Employee employee = new Employee();
		employee.setUserType(role);
		return employee;
	}

	private static LeaveApplication app(String status, Double workingDays) {
		LeaveApplication app = new LeaveApplication();
		app.setStatus(status);
		app.setWorkingDays(workingDays);
		return app;
	}

	// ── mayActOn: status gate per role ───────────────────────────────────────

	@Test
	void adminAndHrMayActAtEveryStatus() {
		for (String role : List.of(Roles.ADMIN, Roles.HR)) {
			for (String status : LeaveConstants.PENDING_STATUSES) {
				assertThat(LeaveWorkflowController.mayActOn(actor(role), app(status, 3.0)))
						.as(role + " at " + status).isTrue();
			}
			assertThat(LeaveWorkflowController.mayActOn(actor(role),
					app(LeaveConstants.STATUS_APPROVED, 3.0))).isTrue();
		}
	}

	@Test
	void supervisorMayActOnlyAtForEndorsement() {
		Employee supervisor = actor(Roles.SUPERVISOR);
		for (String status : LeaveConstants.PENDING_STATUSES) {
			assertThat(LeaveWorkflowController.mayActOn(supervisor, app(status, 3.0)))
					.as("supervisor at " + status)
					.isEqualTo(LeaveConstants.STATUS_FOR_ENDORSEMENT.equals(status));
		}
	}

	@Test
	void councilMayActOnlyAtCouncilReview() {
		Employee council = actor(Roles.COUNCIL);
		for (String status : LeaveConstants.PENDING_STATUSES) {
			assertThat(LeaveWorkflowController.mayActOn(council, app(status, 20.0)))
					.as("council at " + status)
					.isEqualTo(LeaveConstants.STATUS_FOR_COUNCIL_REVIEW.equals(status));
		}
	}

	@Test
	void viceMayorMayActOnlyAtFinalApproval() {
		Employee viceMayor = actor(Roles.VICEMAYOR);
		for (String status : LeaveConstants.PENDING_STATUSES) {
			assertThat(LeaveWorkflowController.mayActOn(viceMayor, app(status, 8.0)))
					.as("vice-mayor at " + status)
					.isEqualTo(LeaveConstants.STATUS_FOR_FINAL_APPROVAL.equals(status));
		}
	}

	@Test
	void employeeAndNullActorMayNeverAct() {
		for (String status : LeaveConstants.PENDING_STATUSES) {
			assertThat(LeaveWorkflowController.mayActOn(actor(Roles.EMPLOYEE), app(status, 3.0))).isFalse();
			assertThat(LeaveWorkflowController.mayActOn(null, app(status, 3.0))).isFalse();
		}
	}

	// ── allowedActions: new FOR_ENDORSEMENT stage ────────────────────────────

	@Test
	void filedOffersForwardToSupervisorEndorseAndReturn() {
		assertThat(controller.allowedActions(app(LeaveConstants.STATUS_FILED, 3.0)))
				.containsExactly(LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR,
						LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_RETURN);
		assertThat(controller.allowedActions(app(LeaveConstants.STATUS_APPEALED, 3.0)))
				.containsExactly(LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR,
						LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_RETURN);
	}

	@Test
	void forEndorsementOffersEndorseAndDisapprove() {
		assertThat(controller.allowedActions(app(LeaveConstants.STATUS_FOR_ENDORSEMENT, 3.0)))
				.containsExactly(LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_DISAPPROVE);
	}

	@Test
	void endorsedStillSplitsOnFiveDays() {
		assertThat(controller.allowedActions(app(LeaveConstants.STATUS_ENDORSED, 5.0)))
				.containsExactly(LeaveConstants.ACTION_APPROVE, LeaveConstants.ACTION_DISAPPROVE);
		assertThat(controller.allowedActions(app(LeaveConstants.STATUS_ENDORSED, 6.0)))
				.containsExactly(LeaveConstants.ACTION_SEND_TO_ADMIN_REVIEW, LeaveConstants.ACTION_DISAPPROVE);
	}

	@Test
	void allowedActionsForFiltersByRole() {
		LeaveApplication pending = app(LeaveConstants.STATUS_FOR_ENDORSEMENT, 3.0);
		assertThat(controller.allowedActionsFor(pending, actor(Roles.SUPERVISOR)))
				.containsExactly(LeaveConstants.ACTION_ENDORSE, LeaveConstants.ACTION_DISAPPROVE);
		assertThat(controller.allowedActionsFor(pending, actor(Roles.VICEMAYOR))).isEmpty();
		assertThat(controller.allowedActionsFor(pending, actor(Roles.EMPLOYEE))).isEmpty();
		assertThat(controller.allowedActionsFor(app(LeaveConstants.STATUS_FOR_FINAL_APPROVAL, 8.0),
				actor(Roles.VICEMAYOR)))
				.containsExactly(LeaveConstants.ACTION_FINAL_APPROVE, LeaveConstants.ACTION_DISAPPROVE);
	}

	// ── queue statuses per role ──────────────────────────────────────────────

	@Test
	void actionableStatusesMatchEachRolesStage() {
		assertThat(LeaveWorkflowController.actionableStatusesFor(actor(Roles.SUPERVISOR)))
				.containsExactly(LeaveConstants.STATUS_FOR_ENDORSEMENT);
		assertThat(LeaveWorkflowController.actionableStatusesFor(actor(Roles.COUNCIL)))
				.containsExactly(LeaveConstants.STATUS_FOR_COUNCIL_REVIEW);
		assertThat(LeaveWorkflowController.actionableStatusesFor(actor(Roles.VICEMAYOR)))
				.containsExactly(LeaveConstants.STATUS_FOR_FINAL_APPROVAL);
		assertThat(LeaveWorkflowController.actionableStatusesFor(actor(Roles.HR)))
				.isEqualTo(LeaveConstants.PENDING_STATUSES);
		assertThat(LeaveWorkflowController.actionableStatusesFor(actor(Roles.ADMIN)))
				.isEqualTo(LeaveConstants.PENDING_STATUSES);
		assertThat(LeaveWorkflowController.actionableStatusesFor(actor(Roles.EMPLOYEE))).isEmpty();
		assertThat(LeaveWorkflowController.actionableStatusesFor(null)).isEmpty();
	}

	// ── stage notifications route to the right role ──────────────────────────

	@Test
	void stageRoleMapsQueueStatusesToActorRoles() {
		assertThat(LeaveWorkflowController.stageRole(LeaveConstants.STATUS_FOR_ENDORSEMENT))
				.isEqualTo(Roles.SUPERVISOR);
		assertThat(LeaveWorkflowController.stageRole(LeaveConstants.STATUS_FOR_COUNCIL_REVIEW))
				.isEqualTo(Roles.COUNCIL);
		assertThat(LeaveWorkflowController.stageRole(LeaveConstants.STATUS_FOR_FINAL_APPROVAL))
				.isEqualTo(Roles.VICEMAYOR);
		assertThat(LeaveWorkflowController.stageRole(LeaveConstants.STATUS_FOR_ADMIN_REVIEW)).isNull();
		assertThat(LeaveWorkflowController.stageRole(LeaveConstants.STATUS_APPROVED)).isNull();
	}

	// ── invariants carried over from v1 ──────────────────────────────────────

	@Test
	void pendingStatusesIncludeForEndorsement() {
		assertThat(LeaveConstants.PENDING_STATUSES).contains(LeaveConstants.STATUS_FOR_ENDORSEMENT);
	}

	@Test
	void forwardToSupervisorNeedsNoSignatoryButEndorseDoes() {
		assertThat(LeaveWorkflowController.requiresSignatory(LeaveConstants.ACTION_FORWARD_TO_SUPERVISOR)).isFalse();
		assertThat(LeaveWorkflowController.requiresSignatory(LeaveConstants.ACTION_ENDORSE)).isTrue();
		assertThat(LeaveWorkflowController.requiresRemarks(LeaveConstants.ACTION_DISAPPROVE)).isTrue();
	}
}
