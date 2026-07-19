package com.ian.web.notification;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

/** Creates in-app notifications (CR Request ID 016). */
@Component
@RequiredArgsConstructor
public class Notifier {

	private final NotificationRepository notificationRepository;
	private final EmployeeRepository employeeRepository;

	public void notify(Employee recipient, String message, String link) {
		if (recipient == null) {
			return;
		}
		Notification notification = new Notification();
		notification.setEmployee(recipient);
		notification.setMessage(message);
		notification.setLink(link);
		notification.setReadFlag(false);
		notification.setCreatedAt(LocalDateTime.now());
		notificationRepository.save(notification);
	}

	/** CR 016 v2: notifies every account holding the given login role. */
	public void notifyRole(String role, String message, String link) {
		if (role == null || role.isBlank()) {
			return;
		}
		for (Employee recipient : employeeRepository.findByUserType(role)) {
			notify(recipient, message, link);
		}
	}
}
