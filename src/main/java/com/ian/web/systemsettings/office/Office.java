package com.ian.web.systemsettings.office;

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
@Table(name = "office")
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
public class Office {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank(message = " is mandatory.")
    private String officeName;
    @Builder.Default
    private boolean isActive = true;
}
