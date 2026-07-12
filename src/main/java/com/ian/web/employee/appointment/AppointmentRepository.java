package com.ian.web.employee.appointment;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

	List<Appointment> findByEmployeeId(long employeeId);
	boolean existsByPositionTitleId(Long positionTitleId);

}
