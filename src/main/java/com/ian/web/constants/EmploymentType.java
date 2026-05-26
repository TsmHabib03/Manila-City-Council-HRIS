package com.ian.web.constants;

public enum EmploymentType {
    PLANTILLA("Plantilla"),
    NON_PLANTILLA("Non-Plantilla");

    final String displayEmployeeType;

    EmploymentType(String displayEmployeeType){
        this.displayEmployeeType = displayEmployeeType;
    }

    EmploymentType valueOf(EmploymentType employmentType) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'valueOf'");
    }


}
