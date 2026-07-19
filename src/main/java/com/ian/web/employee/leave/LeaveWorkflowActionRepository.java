package com.ian.web.employee.leave;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveWorkflowActionRepository extends JpaRepository<LeaveWorkflowAction, Long> {

	List<LeaveWorkflowAction> findByLeaveApplicationIdOrderByCreatedAtAscIdAsc(long leaveApplicationId);

	void deleteByLeaveApplicationId(long leaveApplicationId);

}
