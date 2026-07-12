package com.ian.web.systemsettings.leave_type;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaveTypeRepository extends JpaRepository<LeaveType, Long> {

    List<LeaveType> findAllByOrderByIdAsc();

    List<LeaveType> findByIsActiveTrueOrderByIdAsc();

    boolean existsByLeaveTypeNameIgnoreCase(String leaveTypeName);
}
