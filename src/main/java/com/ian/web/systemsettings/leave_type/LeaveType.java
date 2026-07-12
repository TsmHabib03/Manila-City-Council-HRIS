package com.ian.web.systemsettings.leave_type;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "leave_type")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class LeaveType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String leaveTypeName;
    /** Legal basis / citation printed beside the type on CS Form 6, optional. */
    private String legalBasis;
    @Builder.Default
    private boolean isActive = true;
}
