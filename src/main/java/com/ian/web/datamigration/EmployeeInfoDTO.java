package com.ian.web.datamigration;

import javax.persistence.MappedSuperclass;



import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor 
@AllArgsConstructor
@MappedSuperclass
@Data
public class EmployeeInfoDTO {
	private String emp_no;
    private String plantilla_no;
    private String prefix;
    private String first_name;
    private String middle_name;
    private String last_name;
    private String suffix;
    private String birthdate;
    private String birth_place;
    private String gender;
    private String division;
    private String district;
    private String employee_status;
    private String position_title;
    private String assumptiondate;
    private String citizenship;
    private String country_of_origin;
    private String civil_status;
    private String height;
    private String weight;
    private String blood_type;
    private String gsis_id_no;
    private String pagibig_no;
    private String philhealth_no;
    private String sss_no;
    private String tin;
    private String tel_no;
    private String mobileno1;
    private String email1;
    private String houseno1;
    private String street1;
    private String subdivision1;
    private String brgy1;
    private String city1;
    private String province1;
    private String zipcode1;
}
