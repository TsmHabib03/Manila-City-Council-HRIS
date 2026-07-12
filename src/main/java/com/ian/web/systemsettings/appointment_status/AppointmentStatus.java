package com.ian.web.systemsettings.appointment_status;

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
@Table(name = "appointment_status")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class AppointmentStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String appointmentStatusName;
    @Builder.Default
    private boolean isActive = true;
}
