package com.ian.web.reports;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RaiReportDto {

	private String agency;
    private String resolutionNo;
    private String cscOfficer;
    private String receivedDate;
    
    private String month;
    private String year;
    
    private String signatory1;
    private String signatory2;
    private String signatory3;
    private String signatory4;
    private String signatory5;
    
    private String position1;
    private String position2;
    private String position3;
    private String position4;
    private String position5;
    
    private String hrmoComment1;
    private String hrmoComment2;
    private String hrmoComment3;
    private String hrmoComment4;
    private String hrmoComment5;
    private String hrmoComment6;
    private String hrmoComment7;
    
    private String cscfoComment1;
    private String cscfoComment2;
    private String cscfoComment3;
    private String cscfoComment4;
    private String cscfoComment5;
    private String cscfoComment6;
    private String cscfoComment7;
}
