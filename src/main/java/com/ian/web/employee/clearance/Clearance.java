package com.ian.web.employee.clearance;

import java.time.LocalDate;
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
public class Clearance {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

    private String addressTo;
    private String purpose;
    private String otherPurpose;
    private String status;

    private String approvedBy;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate transDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate effectiveDate;

    @ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;

    @Transient
	private String showMode;

}
