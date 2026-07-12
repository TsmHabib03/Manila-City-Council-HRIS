package com.ian.web.systemsettings.holiday;

import java.time.LocalDate;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Table(name = "holiday")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Holiday {

    public static final String TYPE_REGULAR = "Regular Holiday";
    public static final String TYPE_SPECIAL_NON_WORKING = "Special (Non-Working) Day";
    public static final String TYPE_SPECIAL_WORKING = "Special (Working) Day";
    public static final String TYPE_LOCAL = "Local Holiday (Manila)";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String holidayName;
    @NotNull(message = " is mandatory.")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate holidayDate;
    @NotBlank(message = " is mandatory.")
    private String holidayType;
    private String remarks;
}
