package com.ian.web.notification;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.ian.web.employee.Employee;

import lombok.RequiredArgsConstructor;

/**
 * In-app notifications for the navbar bell (CR Request ID 016). Both endpoints
 * are scoped to the logged-in user via the session actorObj — no id parameters
 * are accepted, so one user can never read another user's notifications.
 */
@Controller
@RequiredArgsConstructor
public class NotificationController {

	private static final DateTimeFormatter NOTIF_TIME = DateTimeFormatter.ofPattern("MMM d, yyyy HH:mm");

	private final NotificationRepository notificationRepository;

	@GetMapping("/my-notifications")
	@ResponseBody
	public Map<String, Object> myNotifications(HttpServletRequest request) {
		Employee actor = (Employee) request.getSession().getAttribute("actorObj");
		Map<String, Object> result = new HashMap<>();
		List<Map<String, Object>> items = new ArrayList<>();
		if (actor == null) {
			result.put("unread", 0);
			result.put("items", items);
			return result;
		}
		result.put("unread", notificationRepository.countByEmployeeIdAndReadFlagFalse(actor.getId()));
		for (Notification n : notificationRepository
				.findTop15ByEmployeeIdOrderByCreatedAtDescIdDesc(actor.getId())) {
			Map<String, Object> item = new LinkedHashMap<>();
			item.put("message", n.getMessage());
			item.put("link", n.getLink());
			item.put("read", n.isReadFlag());
			item.put("createdAt", n.getCreatedAt() != null ? NOTIF_TIME.format(n.getCreatedAt()) : "");
			items.add(item);
		}
		result.put("items", items);
		return result;
	}

	@PostMapping("/my-notifications/mark-read")
	@ResponseBody
	@Transactional
	public Map<String, Object> markAllRead(HttpServletRequest request) {
		Employee actor = (Employee) request.getSession().getAttribute("actorObj");
		Map<String, Object> result = new HashMap<>();
		result.put("marked", actor != null ? notificationRepository.markAllRead(actor.getId()) : 0);
		return result;
	}
}
