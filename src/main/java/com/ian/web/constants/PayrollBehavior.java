package com.ian.web.constants;

public enum PayrollBehavior {
    DATE_RATE("Daily Rate"),
    MONTHLY_RATE("Monthly Rate");

    private final String payrollBehaviorName;

    PayrollBehavior(String payrollBehaviorName){
        this.payrollBehaviorName = payrollBehaviorName;
    }
}
