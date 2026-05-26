package com.ian.web.employee;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.ian.web.common.model.Person;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.systemsettings.district.District;
import com.ian.web.systemsettings.division.Division;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.position_title.PositionTitle;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "unique_username_employee", columnNames = "username")
})
@NoArgsConstructor 
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Employee extends Person  implements UserDetails {
	
	private static final long serialVersionUID = 1L;
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
	
	@Transient
	private String showMode;
	
	private String empHashCode;
	private String empNo;
	private String username;
	private String password;
	
	@Transient
	private String confirmPassword;

	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private LocalDate assumptiondate;	
    
    private String plantillaNo;
    private String titleSuffix;
    private String civilStatus;
    private String height;
    private String weight;
    private String religion;
    private String bloodType;
    private String gsisBpNo;
    private String gsisPolicyNo;
    private String gsisIdNo;
    private String pagibigNo;
    private String philhealthNo;
    private String sssNo;
    private String tin;
    private String umidNo;
    private String philsysNo;
    private String citizenship;
    private String countryOfOrigin;
    private String birthPlace;
    private String telNo;
    
    @Email(message = "Invalid email.")
    private String email1;	
    @Email(message = "Invalid email.")
    private String email2;
    
    private String mobileNo1;
    private String mobileNo2;
	
    private String status = "ACTIVE";
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "district_id")
    private District district;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "division_id")
    private Division division;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "employee_status_id")
    private EmployeeStatus employeeStatus;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "position_title_id")
    private PositionTitle positionTitle;
    
    @NotBlank
    private String userType;
    
    private String profilePhoto;
    
    
    private String houseno1;
    private String houseno2;
    private String street1;
    private String street2;
    private String subdivision1;
    private String subdivision2;
    private String brgy1;
    private String brgy2;
    private String city1;
    private String city2;
    private String province1;
    private String province2;
    private String zipcode1;
    private String zipcode2;    
	
	@Transient
	private MultipartFile photoFile;
	
	@Transient
	private List<FamilyBg> familyBgList;
	
	@Transient
	private PdsCountDto pdsCountDto;
    
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        authorities.add(new SimpleGrantedAuthority(this.userType));
        return authorities;
    }
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }
    @Override
    public boolean isEnabled() {
        return true;
    }

    public String getUserTypeDisplay() {
        if (this.userType == null) {
            return "";
        }
        return this.userType.replace("ROLE_", "");
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Employee{");
        sb.append("id=").append(id);
        sb.append(", showMode='").append(showMode).append('\'');
        sb.append(", empHashCode='").append(empHashCode).append('\'');
        sb.append(", empNo='").append(empNo).append('\'');
        sb.append(", username='").append(username).append('\'');
        sb.append(", assumptiondate=").append(assumptiondate);
        sb.append(", plantillaNo='").append(plantillaNo).append('\'');
        sb.append(", titleSuffix='").append(titleSuffix).append('\'');
        sb.append(", civilStatus='").append(civilStatus).append('\'');
        sb.append(", height='").append(height).append('\'');
        sb.append(", weight='").append(weight).append('\'');
        sb.append(", religion='").append(religion).append('\'');
        sb.append(", bloodType='").append(bloodType).append('\'');
        sb.append(", gsisBpNo='").append(gsisBpNo).append('\'');
        sb.append(", gsisPolicyNo='").append(gsisPolicyNo).append('\'');
        sb.append(", gsisIdNo='").append(gsisIdNo).append('\'');
        sb.append(", pagibigNo='").append(pagibigNo).append('\'');
        sb.append(", philhealthNo='").append(philhealthNo).append('\'');
        sb.append(", sssNo='").append(sssNo).append('\'');
        sb.append(", tin='").append(tin).append('\'');
        sb.append(", umidNo='").append(umidNo).append('\'');
        sb.append(", philsysNo='").append(philsysNo).append('\'');
        sb.append(", citizenship='").append(citizenship).append('\'');
        sb.append(", countryOfOrigin='").append(countryOfOrigin).append('\'');
        sb.append(", birthPlace='").append(birthPlace).append('\'');
        sb.append(", telNo='").append(telNo).append('\'');
        sb.append(", email1='").append(email1).append('\'');
        sb.append(", email2='").append(email2).append('\'');
        sb.append(", mobileNo1='").append(mobileNo1).append('\'');
        sb.append(", mobileNo2='").append(mobileNo2).append('\'');
        sb.append(", status='").append(status).append('\'');
        sb.append(", district=").append(district);
        sb.append(", division=").append(division);
        sb.append(", employeeStatus=").append(employeeStatus);
        sb.append(", positionTitle=").append(positionTitle);
        sb.append(", userType='").append(userType).append('\'');
        sb.append(", profilePhoto='").append(profilePhoto).append('\'');
        sb.append(", houseno1='").append(houseno1).append('\'');
        sb.append(", houseno2='").append(houseno2).append('\'');
        sb.append(", street1='").append(street1).append('\'');
        sb.append(", street2='").append(street2).append('\'');
        sb.append(", subdivision1='").append(subdivision1).append('\'');
        sb.append(", subdivision2='").append(subdivision2).append('\'');
        sb.append(", brgy1='").append(brgy1).append('\'');
        sb.append(", brgy2='").append(brgy2).append('\'');
        sb.append(", city1='").append(city1).append('\'');
        sb.append(", city2='").append(city2).append('\'');
        sb.append(", province1='").append(province1).append('\'');
        sb.append(", province2='").append(province2).append('\'');
        sb.append(", zipcode1='").append(zipcode1).append('\'');
        sb.append(", zipcode2='").append(zipcode2).append('\'');
        sb.append(", photoFile=").append(photoFile);
        sb.append(", familyBgList=").append(familyBgList);
        sb.append(", pdsCountDto=").append(pdsCountDto);
        sb.append('}');
        return sb.toString();
    }
	
}
