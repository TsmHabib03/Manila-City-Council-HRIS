package com.ian.web.employee.training;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Training and Seminar record under Employee Management (CR Request ID 015,
 * 2026-07-16): type/title of training, attendees, date, provider, location.
 */
@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class TrainingSeminar {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	/** e.g. Technical Skills, Soft Skills */
	private String typeOfTraining;

	private String titleOfTraining;

	/** Free-text list of attendees, e.g. "John, Maria, Alex". */
	private String employeeAttendees;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate trainingDate;

	private String trainingProvider;

	private String location;
}
