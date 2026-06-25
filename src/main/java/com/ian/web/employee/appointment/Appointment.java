package com.ian.web.employee.appointment;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.employee.Employee;
import com.ian.web.systemsettings.position_title.PositionTitle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Appointment {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	private String plantillaNo;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate signingDate;

	private int pageNo;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_title_id")
    private PositionTitle positionTitle;

	private String status;
	private Double salary;
	private String vice;
	private String statusOfSepeparation;
	private String statusOfAppointment;
	private int salaryGrade;
	private int stepInc;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate entranceDate;

	private String eligibility;
	private String highestEducAttainment;
	private String officeAssignment;
	private String remarks;
	private String district;
	private String experience;
	private String training;

}
