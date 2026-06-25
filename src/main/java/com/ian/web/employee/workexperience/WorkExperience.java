package com.ian.web.employee.workexperience;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
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
import com.ian.web.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "work_experience")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkExperience {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateFrom;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate dateTo;
	
	private boolean upToPresent;

	private boolean partTime;

	@NotBlank
    private String positionTitle;
	private String department;
	private String officeName;
	private String immediateSupervisor;
	private String jobDescription;
	
	private BigDecimal salary;
	private int salaryGrade;
	private int stepNo;
	private String appointmentStatus;
	private String govtOffice;
	
	private String remarks;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;

	@Transient
	private String showMode;

	public String getInclusiveDates(){
		return this.dateFrom +" - "+this.dateTo;
	}
	
	public String getFormattedSalary(){
		DecimalFormat decimalFormat = new DecimalFormat("#,###,###.00");
		return decimalFormat.format(this.salary);
	}
}