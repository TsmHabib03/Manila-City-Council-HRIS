package com.ian.web.employee.leave;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveApplicationRepository extends JpaRepository<LeaveApplication, Long> {

	List<LeaveApplication> findByEmployeeIdOrderByDateOfFilingDesc(long employeeId);

}
