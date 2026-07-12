package com.ian.web.employee.leave;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveCardEntryRepository extends JpaRepository<LeaveCardEntry, Long> {

	List<LeaveCardEntry> findByEmployeeIdOrderByEntryDateAscIdAsc(long employeeId);

	List<LeaveCardEntry> findByLeaveApplicationId(long leaveApplicationId);

	boolean existsByEmployeeIdAndEntryTypeAndPeriod(long employeeId, String entryType, String period);

}
