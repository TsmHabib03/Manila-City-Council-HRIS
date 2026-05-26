package com.ian.web.systemsettings.employee_status;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeStatusRepository extends JpaRepository<EmployeeStatus, Long> {
	Optional<EmployeeStatus> findByEmployeeStatusName(String employeeStatusName);    
}
