package com.ian.web.systemsettings.division;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import com.ian.web.employee.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "division")
@Entity
public class Division {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	
    @NotBlank(message = " is mandatory.")
    private String divisionName;
    
    private int orderNo;
    
    @ManyToOne
    @JoinColumn(name = "approver1_id")
    private Employee approver1;
    
    @ManyToOne
    @JoinColumn(name = "approver2_id")
    private Employee approver2;

}
