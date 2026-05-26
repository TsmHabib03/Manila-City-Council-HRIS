package com.ian.web.reports;

import lombok.Data;

@Data
public class EmpCertificateDto {
	
	private int empId;
	private String empName;
	private String empStatus;
	private String position;
	private String rate;
	private String signatory;
	private String signatoryPosition;

}
