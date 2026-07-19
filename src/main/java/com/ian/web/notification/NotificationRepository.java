package com.ian.web.notification;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

	List<Notification> findTop15ByEmployeeIdOrderByCreatedAtDescIdDesc(long employeeId);

	long countByEmployeeIdAndReadFlagFalse(long employeeId);

	@Modifying
	@Query("update Notification n set n.readFlag = true where n.employee.id = :employeeId and n.readFlag = false")
	int markAllRead(@Param("employeeId") long employeeId);

}
