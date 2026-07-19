package com.ian.web.employee.leave;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;

/**
 * CR 016 v2: the year-end mandatory deduction excludes coterminous employees;
 * everything else (idempotency, partial-use proration) stays v1.
 */
class LeaveYearEndProcessorTest {

	private EmployeeRepository employeeRepository;
	private LeaveApplicationRepository leaveApplicationRepository;
	private LeaveCardEntryRepository leaveCardEntryRepository;
	private LeaveYearEndProcessor processor;

	@BeforeEach
	void setUp() {
		employeeRepository = mock(EmployeeRepository.class);
		leaveApplicationRepository = mock(LeaveApplicationRepository.class);
		leaveCardEntryRepository = mock(LeaveCardEntryRepository.class);
		processor = new LeaveYearEndProcessor(
				employeeRepository, leaveApplicationRepository, leaveCardEntryRepository);
	}

	private static Employee employee(long id, String status, String employeeStatusName) {
		Employee employee = new Employee();
		employee.setId(id);
		employee.setStatus(status);
		if (employeeStatusName != null) {
			employee.setEmployeeStatus(EmployeeStatus.builder()
					.employeeStatusName(employeeStatusName).build());
		}
		return employee;
	}

	// ── isCoterminous ────────────────────────────────────────────────────────

	@Test
	void isCoterminousMatchesSpellingVariants() {
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", "COTERMINOUS"))).isTrue();
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", "CO-TERMINOUS"))).isTrue();
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", "Co Terminous"))).isTrue();
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", "co-terminous"))).isTrue();
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", "PERMANENT"))).isFalse();
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", "CASUAL"))).isFalse();
		assertThat(LeaveYearEndProcessor.isCoterminous(employee(1, "ACTIVE", null))).isFalse();
		assertThat(LeaveYearEndProcessor.isCoterminous(null)).isFalse();
	}

	// ── run(): coterminous exclusion ─────────────────────────────────────────

	@Test
	void runSkipsCoterminousAndDeductsOthers() {
		Employee coterminous = employee(1, "ACTIVE", "COTERMINOUS");
		Employee permanent = employee(2, "ACTIVE", "PERMANENT");
		when(employeeRepository.findAll()).thenReturn(List.of(coterminous, permanent));
		when(leaveCardEntryRepository.existsByEmployeeIdAndEntryTypeAndPeriod(
				anyLong(), anyString(), anyString())).thenReturn(false);
		when(leaveApplicationRepository.findByEmployeeIdAndLeaveTypeAndStatusOrderByDateFromAsc(
				anyLong(), anyString(), anyString())).thenReturn(List.of());

		String summary = processor.run(2026);

		ArgumentCaptor<LeaveCardEntry> captor = ArgumentCaptor.forClass(LeaveCardEntry.class);
		verify(leaveCardEntryRepository).save(captor.capture());
		assertThat(captor.getValue().getEmployee().getId()).isEqualTo(2);
		assertThat(captor.getValue().getVlDeducted()).isEqualTo(5.0);
		assertThat(captor.getValue().getPeriod()).isEqualTo("YE-2026");
		assertThat(summary).contains("1 employee(s) deducted").contains("1 coterminous (excluded)");
	}

	@Test
	void runIsIdempotentAcrossRepeatedRuns() {
		Employee permanent = employee(2, "ACTIVE", "PERMANENT");
		when(employeeRepository.findAll()).thenReturn(List.of(permanent));
		when(leaveCardEntryRepository.existsByEmployeeIdAndEntryTypeAndPeriod(
				anyLong(), anyString(), anyString())).thenReturn(true);

		String summary = processor.run(2026);

		verify(leaveCardEntryRepository, never()).save(any());
		assertThat(summary).contains("0 employee(s) deducted").contains("1 already processed");
	}

	@Test
	void runSkipsInactiveEmployees() {
		Employee separated = employee(3, "RESIGNED", "PERMANENT");
		Employee serviceAccount = employee(4, "N/A", null);
		when(employeeRepository.findAll()).thenReturn(List.of(separated, serviceAccount));

		String summary = processor.run(2026);

		verify(leaveCardEntryRepository, never()).save(any());
		assertThat(summary).contains("0 employee(s) deducted");
	}
}
