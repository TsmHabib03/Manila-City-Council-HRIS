package com.ian.web.employee.leave;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

	List<LeaveApplication> findByEmployeeIdOrderByDateOfFilingDesc(long employeeId);

	List<LeaveApplication> findByStatusOrderByDateOfFilingDesc(String status);

	long countByStatus(String status);

	List<LeaveApplication> findByStatusInOrderByDateOfFilingDesc(Collection<String> statuses);

	long countByStatusIn(Collection<String> statuses);

	/** Year-end mandatory deduction: an employee's approved leaves of one type (CR 016). */
	List<LeaveApplication> findByEmployeeIdAndLeaveTypeAndStatusOrderByDateFromAsc(
			long employeeId, String leaveType, String status);

}
