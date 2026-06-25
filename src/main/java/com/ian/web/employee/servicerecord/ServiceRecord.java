package com.ian.web.employee.servicerecord;

import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.springframework.format.annotation.DateTimeFormat;

import com.ian.web.employee.Employee;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.position_title.PositionTitle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor 
@AllArgsConstructor
public class ServiceRecord {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
		
    private String isPresent;
    
    private String designation;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_status_id")
    private EmployeeStatus employeeStatus;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_title_id")
    private PositionTitle positionTitle;
    
    private String levelOfPosition;
    
    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;
    
    private Double salary;
    
    private String station;
    
    private String branch;
    
    private String lvAbs;
    
    //mode of separation
    private String separationCause;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate separationDate;
    
    @Transient
    private String showMode;
    
    //For AppoIntegerment    
    private String plantillaNo;  
    
    @Column(name = "page_no", nullable = true)
    private Integer pageNo;
    private String employmentStatusNotes;
    private String positionTitleNotes;
    private String vice;
    private String statusOfSepeparation;
    
    //This is nature of appointment or mode of accession
    private String statusOfAppointment;
    
    @Column(name = "salary_grade", nullable = true)
    private Integer salaryGrade;
    
    @Column(name = "step_inc", nullable = true)
    private Integer stepInc;
    
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate signingDate;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate entranceDate;
	private String eligibility;
	private String officeAssignment;
	private String remarks;
	private String district;
	private String experience;
	private String training;
	
	
	//New Field because of RAI		
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate publicationDateTo;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate publicationDateFrom;
	
	private String modeOfPublication;
	
	private String validateInv;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfActionCscAction;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateOfReleaseCscAction;
	
	private String agencyReceivingOfficer;

//	public LocalDate getSigningDate() {
//		if(signingDate != null) {
//			return signingDate.plusDays(1);
//		} else {
//			return signingDate;
//		}
//	}
//	
//	public LocalDate getEntranceDate() {
//		if(entranceDate != null) {
//			return entranceDate.plusDays(1);
//		} else {
//			return entranceDate;
//		}
//	}
//	
//	//separationDate, dateFrom, dateTo
//	public LocalDate getSeparationDate() {
//		if(separationDate != null) {
//			return separationDate.plusDays(1);
//		} else {
//			return separationDate;
//		}
//	}
//	
//	public LocalDate getDateFrom() {
//		if(dateFrom != null) {
//			return dateFrom.plusDays(1);
//		} else {
//			return dateFrom;
//		}
//	}
//	
//	public LocalDate getDateTo() {
//		if(dateTo != null) {
//			return dateTo.plusDays(1);
//		} else {
//			return dateTo;
//		}
//	}

}
