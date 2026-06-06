package com.ian.web.employee.familybg;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import com.ian.web.common.model.Person;
import com.ian.web.employee.Employee;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FamilyBg extends Person {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
		
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	Employee employee;
	
	private String relationship;
	private boolean isDeceased;
	private String tin;
	private String occupation;
	private String employer;
	private String telNo;
	private String businessAdd;
	private String employmentStatus;
	private String mobileNo;
	private String maidenName;

	@Transient
	private String showMode;
	
//	@OneToMany(mappedBy = "familyBg", cascade = CascadeType.ALL)
//    private List<FamilyRelative> relatives;

}
