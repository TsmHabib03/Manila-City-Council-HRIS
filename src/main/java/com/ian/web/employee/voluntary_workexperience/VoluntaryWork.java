package com.ian.web.employee.voluntary_workexperience;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "voluntary_work")
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VoluntaryWork {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@NotBlank
    private String orgName;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	private boolean upToPresent;
	
	private int noHours;
	
	private String natureOfWork;
	
	private String address;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;
	
	@Transient
	private String showMode;

	public String getInclusiveDates(){
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
		String from = dateFrom != null ? formatter.format(dateFrom) : "N/A";
		String to = dateTo != null ? formatter.format(dateTo) : (upToPresent ? "Present" : "N/A");
		return from + " - " + to;
	}
	
}
