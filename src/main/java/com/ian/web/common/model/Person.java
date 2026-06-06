package com.ian.web.common.model;

import java.time.LocalDate;
import java.time.Period;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.ian.web.config.data.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor
@MappedSuperclass
@Data
public class Person extends Auditable {
	
	@NotBlank(message = " is mandatory.")
    private String firstName;
	
	@NotBlank(message = " is mandatory.")
    private String lastName;
	
	private String middleName;
	
	private String prefix;
	private String suffix;
	private String birthPlace;
	
	
	@NotBlank(message = " is mandatory.")
    private String gender;
		
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate birthdate;
	
	
	public LocalDate getBirthdate() {
		return birthdate;
	}
    
    public String getFullName() {
    	StringBuilder displayName = new StringBuilder();

        if (firstName != null && !firstName.isEmpty()) {
            displayName.append(firstName).append(" ");
        }

        if (middleName != null && !middleName.isEmpty()) {
            displayName.append(middleName.charAt(0)).append(". ");
        }

        if (lastName != null && !lastName.isEmpty()) {
            displayName.append(lastName).append(" ");
        }

        if (suffix != null && !suffix.isEmpty()) {
            displayName.append(suffix);
        }

        return displayName.toString().trim();
    }
    
    public String getDisplayName() {
    	StringBuilder displayName = new StringBuilder();

        if (prefix != null && !prefix.isEmpty()) {
            displayName.append(prefix).append(" ");
        }

        if (firstName != null && !firstName.isEmpty()) {
            displayName.append(firstName).append(" ");
        }

        if (middleName != null && !middleName.isEmpty()) {
            displayName.append(middleName.charAt(0)).append(". ");
        }

        if (lastName != null && !lastName.isEmpty()) {
            displayName.append(lastName).append(" ");
        }

        if (suffix != null && !suffix.isEmpty()) {
            displayName.append(suffix);
        }

        return displayName.toString().trim();
    }
    
    @JsonIgnore
	public Integer getAge() {
		LocalDate currentBirthdate = getBirthdate();
		if (currentBirthdate == null) {
			return null;
		}
		return Period.between(currentBirthdate, LocalDate.now()).getYears();
	}

}
