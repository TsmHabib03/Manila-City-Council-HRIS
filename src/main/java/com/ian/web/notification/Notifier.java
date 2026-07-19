package com.ian.web.notification;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.ian.web.employee.Employee;

import lombok.RequiredArgsConstructor;

/** Creates in-app notifications (CR Request ID 016). */
@Component
@RequiredArgsConstructor
public class Notifier {

	private final NotificationRepository notificationRepository;

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
}
