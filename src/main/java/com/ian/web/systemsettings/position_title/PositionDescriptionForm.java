package com.ian.web.systemsettings.position_title;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;




@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "position_description_form")
@Entity
@Builder
public class PositionDescriptionForm {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relationship to PositionTitle
    @ManyToOne
    @JoinColumn(name = "position_title_id", nullable = false)
    private PositionTitle positionTitle;
    
    private String itemNumber;
    private String salaryGrade;
    
    // LGU Info
    private boolean lguProvince;
    private boolean lguCity;
    private boolean lguMunicipality;
    private boolean lguClass1;
    private boolean lguClass2;
    private boolean lguClass3;
    private boolean lguClass4;
    private boolean lguClass5;
    private boolean lguClass6;
    private boolean lguClassSpecial;
    
    // Agency Info
    private String departmentAgency;
    private String bureauOffice;
    private String branchDivision;
    private String workstation;

    // Appropriation Info
    private String presentAppropriationAct;
    private String previousAppropriationAct;
    private String salaryAuthorized;
    private String otherCompensation;
    
    // Supervisor Info
    private String immediateSupervisorTitle;
    private String nextHigherSupervisorTitle;

    // Supervised Staff Info
    private String supervisedStaffPositionTitle;
    private String supervisedStaffItemNumber;
    
    @Lob
    private String equipmentUsed;
    
    // Contacts (Consolidated)
    private String contactInternalExec;
    private String contactInternalSupervisor;
    private String contactInternalNonSupervisor;
    private String contactInternalStaff;
    private String contactExternalPublic;
    private String contactExternalAgencies;
    private String contactExternalOthers;
    private String contactExternalOthersText;
    
    // Working Conditions (Consolidated)
    private String workConditionOffice;
    private String workConditionField;
    private String workConditionOthers;
    private String workConditionOthersText;
    
    private String functionOfUnit;
    
    @Lob
    private String jobSummary;

    // Qualification Standards
    @Lob
    private String qsEducation;
    @Lob
    private String qsExperience;
    @Lob
    private String qsTraining;
    @Lob
    private String qsEligibility;

    // Competencies
    @Lob
    private String coreCompetencies;
    private String coreCompetencyLevel;
    @Lob
    private String leadershipCompetencies;
    @Lob
    private String leadershipCompetencyLevel;

    // Duties
    @Lob
    private String dutyDescription;
    @Lob
    private String dutyPercentage;
    @Lob
    private String dutyCompetencyLevel;

    // Signatories
    private String employeeName;
    private String supervisorName;
    
}
