package com.ian.web.systemsettings.position_title;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.levels.Level;
import com.ian.web.systemsettings.position_title.competencies.Competency;
import com.ian.web.systemsettings.salary_grades.SalaryGrade;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "position_titles")
@Entity
@Builder
public class PositionTitle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String positionTitleName;
    
    private String departmentCode;

//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "position_title_employment", referencedColumnName = "id")
//    private EmployeeStatus employeeStatus;
//
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "position_title_level", referencedColumnName = "id")
//    private Level level;
//
//    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
//    @JoinColumn(name = "position_title_salary_grade", referencedColumnName = "id")
//    private SalaryGrade salaryGrade;
    
    @Builder.Default
    private boolean isActive = true;
}
