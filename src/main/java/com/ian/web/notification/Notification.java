package com.ian.web.notification;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * In-app notification shown on the navbar bell (CR Request ID 016).
 * Currently produced by the leave decision flow (approval, disapproval,
 * return, cancel confirmation...) but kept generic for other modules.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** Recipient. */
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	private String message;

	/** Relative link opened when the notification is clicked, e.g. /my-leaves/... */
	private String link;

	private boolean readFlag;

	private LocalDateTime createdAt;
}
