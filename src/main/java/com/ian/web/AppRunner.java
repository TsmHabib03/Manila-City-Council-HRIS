package com.ian.web;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ian.web.datamigration.DataMigrationService;

import lombok.AllArgsConstructor;

//For local testing
@Component
@AllArgsConstructor
public class AppRunner  implements CommandLineRunner {
	
	private final DataMigrationService service;


    @Override
    public void run(String... args) throws Exception {
        
    	
//    	service.migrateEmployeeInfo();
//    	service.migrateFamilyBg();;
//    	service.migrateEducationalBg();
//    	service.migrateWorkExperience();
//    	service.migrateLearning();
//    	service.migrateOtherInfo();
//    	service.migrateGovId();
//    	service.migrateReference();;
//    	service.migrateServiceRecord();
//    	service.generateAllEmphashCode();
    	
    }
}
