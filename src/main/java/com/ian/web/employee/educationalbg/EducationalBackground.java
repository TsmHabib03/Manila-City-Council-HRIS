package com.ian.web.employee.educationalbg;

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

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import com.ian.web.employee.Employee;
import com.ian.web.systemsettings.academichonors.AcademicHonors;
import com.ian.web.systemsettings.degree_courses.DegreeCourses;
import com.ian.web.systemsettings.degreelevels.DegreeLevel;
import com.ian.web.systemsettings.scholarship.Scholarship;
import com.ian.web.systemsettings.schools.School;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "educational_background")
@Entity
public class EducationalBackground {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "degree_level_id")
	private DegreeLevel degreeLevel;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "school_id")
	private School school;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "degree_course_id")
	private DegreeCourses degreeCourse;
	
	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "scholarship_id")
	private Scholarship scholarship;

	@ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "academic_honors_id")
	private AcademicHonors academicHonors;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "employee_id")
	private Employee employee;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate startDate;
	
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate endDate;	
	
	private boolean upToPresent;
	private String unitsEarned;
	private int yearGraduated;	
	private String remarks;
	private String schoolCustomName;

	@Transient
	private String showMode;	
	
	private String attachmentUrl;	
	@Transient
	private MultipartFile attachedFile;	

	public String getEffectiveSchoolName() {
		if (school != null && school.getSchoolName() != null && !school.getSchoolName().isBlank()) {
			return school.getSchoolName();
		}
		return schoolCustomName != null ? schoolCustomName : "";
	}

	public String getDateToString(){
		if(startDate != null && endDate != null) {
			return startDate.getMonth()+" "+startDate.getYear()+" - "+endDate.getMonth()+" "+endDate.getYear();
		} else {
			return "";
		}		
	}
}
