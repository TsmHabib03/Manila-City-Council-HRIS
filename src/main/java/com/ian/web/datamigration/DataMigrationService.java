package com.ian.web.datamigration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.docs201.Docs201Repository;
import com.ian.web.employee.educationalbg.EducationalBackground;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedId;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopment;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfo;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.references.EmpReferences;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.servicerecord.ServiceRecord;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequestRepository;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.employee.workexperience.WorkExperience;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.degree_courses.DegreeCourses;
import com.ian.web.systemsettings.degree_courses.DegreeCoursesRepository;
import com.ian.web.systemsettings.degreelevels.DegreeLevel;
import com.ian.web.systemsettings.degreelevels.DegreeLevelRepository;
import com.ian.web.systemsettings.district.District;
import com.ian.web.systemsettings.district.DistrictRepository;
import com.ian.web.systemsettings.division.Division;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitle;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;
import com.ian.web.systemsettings.schools.School;
import com.ian.web.systemsettings.schools.SchoolRepository;

@Service
public class DataMigrationService {

	private static final Logger log = LoggerFactory.getLogger(DataMigrationService.class);

	@Value("${migration.csv.dir}")
	private String migrationCsvDir;

	@Autowired
	private EmployeeRepository employeeRepository;

	@Autowired
	private DivisionRepository divisionRepository;

	@Autowired
	private DistrictRepository districtRepository;

	@Autowired
	private EmployeeStatusRepository employeeStatusRepository;

	@Autowired
	private PositionTitleRepository positionTitleRepository;

	@Autowired
	private FamilyBgRepository familyBgRepository;
	
	@Autowired
	private DegreeLevelRepository degreeLevelRepository;

	@Autowired
	private DegreeCoursesRepository degreeCoursesRepository;

	@Autowired
	private SchoolRepository schoolRepository;

	@Autowired
	private EducationalBackgroundRepository educationalBackgroundRepository;

	@Autowired
	private ServiceRecordRepository serviceRecordRepository;
	
	@Autowired
	private WorkExperienceRepository workExperienceRepository;
	
	@Autowired
	private LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	
	@Autowired
	private OtherInfoRepository otherInfoRepository;
	
	@Autowired
	private GovermentIssuedIdRepository govermentIssuedIdRepository;
	
	@Autowired
	private EmpReferencesRepository empReferencesRepository;
	
	
	

	/**
	 * Parses a date string with the format "M/d/yyyy".
	 * @param dateStr The date string to parse.
	 * @return A LocalDate object, or null if parsing fails.
	 */
	private LocalDate parseDate(String dateStr) {
		if (dateStr == null || dateStr.trim().isEmpty() || dateStr.trim().equalsIgnoreCase("N/A")) {
			return null;
		}
		try {
			// Define the formatter for "M/d/yyyy" pattern
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
			return LocalDate.parse(dateStr.trim(), formatter);
		} catch (DateTimeParseException e) {
			log.warn("Could not parse date: " + dateStr + ". Error: " + e.getMessage());
			return null;
		}
	}

    private Division getOrCreateDivision(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return null;
        Optional<Division> existing = divisionRepository.findByDivisionName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            Division newDivision = Division.builder().divisionName(name).build();
            return divisionRepository.save(newDivision);
        }
    }

    private District getOrCreateDistrict(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return null;
        Optional<District> existing = districtRepository.findByDistrictName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            District newDistrict = District.builder().districtName(name).build();
            return districtRepository.save(newDistrict);
        }
    }

    private EmployeeStatus getOrCreateEmployeeStatus(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) {
        	//return null;
        	//CREATE "NO EMPLOYEE STATUS"
        	Optional<EmployeeStatus> existing = employeeStatusRepository.findByEmployeeStatusName("NO EMPLOYEE STATUS");
            if (existing.isPresent()) {
                return existing.get();
            } else {
                EmployeeStatus newStatus = EmployeeStatus.builder().employeeStatusName(name).isActive(true).build();
                return employeeStatusRepository.save(newStatus);
            }
        }
        Optional<EmployeeStatus> existing = employeeStatusRepository.findByEmployeeStatusName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            EmployeeStatus newStatus = EmployeeStatus.builder().employeeStatusName(name).isActive(true).build();
            return employeeStatusRepository.save(newStatus);
        }
    }

    private PositionTitle getOrCreatePositionTitle(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return null;
        Optional<PositionTitle> existing = positionTitleRepository.findByPositionTitleName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            PositionTitle newTitle = PositionTitle.builder().positionTitleName(name).isActive(true).build();
            return positionTitleRepository.save(newTitle);
        }
    }
    
    private School getOrCreateSchool(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return null;
        // NOTE: Assumes findBySchoolName exists in SchoolRepository
        Optional<School> existing = schoolRepository.findBySchoolName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            School newSchool = School.builder().schoolName(name).isActive(true).build();
            return schoolRepository.save(newSchool);
        }
    }

    private DegreeCourses getOrCreateDegreeCourse(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return null;
        // NOTE: Assumes findByDegreeCourseName exists in DegreeCoursesRepository
        Optional<DegreeCourses> existing = degreeCoursesRepository.findByDegreeCourseName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            DegreeCourses newCourse = DegreeCourses.builder().degreeCourseName(name).isActive(true).build();
            return degreeCoursesRepository.save(newCourse);
        }
    }

    private DegreeLevel getOrCreateDegreeLevel(String name) {
        if (name == null || name.trim().isEmpty() || name.equalsIgnoreCase("N/A")) return null;
        // NOTE: Assumes findByDegreeName exists in DegreeLevelRepository
        Optional<DegreeLevel> existing = degreeLevelRepository.findByDegreeName(name);
        if (existing.isPresent()) {
            return existing.get();
        } else {
            DegreeLevel newLevel = DegreeLevel.builder().degreeName(name).isActive(true).build();
            return degreeLevelRepository.save(newLevel);
        }
    }
    
    private LocalDate parseYearToDate(String yearStr) {
        if (yearStr == null || yearStr.trim().isEmpty() || !yearStr.trim().matches("\\d{4}")) {
            return null;
        }
        try {
            int year = Integer.parseInt(yearStr.trim());
            return LocalDate.of(year, 1, 1); // Default to the first day of the year
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private int parseIntSafe(String intStr) {
        if (intStr == null || intStr.trim().isEmpty() || !intStr.trim().matches("\\d+")) {
            return 0;
        }
        try {
            return Integer.parseInt(intStr.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private BigDecimal parseBigDecimalSafe(String decimalStr) {
        if (decimalStr == null || decimalStr.trim().isEmpty()) {
            return null;
        }
        try {
            return new BigDecimal(decimalStr.trim().replaceAll(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private Double parseDoubleSafe(String doubleStr) {
        if (doubleStr == null || doubleStr.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(doubleStr.trim().replaceAll("[^\\d.]", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private boolean isNullOrNA(String str) {
        return str == null || str.trim().isEmpty() || str.trim().equalsIgnoreCase("N/A");
    }
    
    private void countCsvPrimaryRecords(String filePath) {
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        int count = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);
                boolean isPrimaryRecord = data.length > 5
                                          && data[3] != null && !data[3].replace("\"", "").trim().isEmpty()
                                          && data[5] != null && !data[5].replace("\"", "").trim().isEmpty();
                
                if (isPrimaryRecord) {
                    count++;
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }
        log.info("==========================================================");
        log.info("Total rows with first_name and last_name: " + count);
        log.info("==========================================================");
    }
    
    @Transactional
	public void generateAllEmphashCode() {
    	log.info("--------------------------------- Start generateAllEmphashCode.......");
		List<Employee> list = employeeRepository.findAll();
		for(Employee e : list) {
			e.setEmpHashCode(generateAlphanumericHash());
			employeeRepository.save(e);
		}
		log.info("--------------------------------- End generateAllEmphashCode.......");
	}

	@Transactional
	public void migrateEmployeeInfo() {
		log.info("Start migrateEmployeeInfo.......");

		String csvFile = migrationCsvDir + "/Manila_City_Council_Data_Template.csv";
		
        countCsvPrimaryRecords(csvFile);

		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        
        long noEmpNoCounter = 1;
        long noPlantillaCounter = 1;

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			br.readLine(); // Skip header line

			Employee lastSavedEmployee = null;

			while ((line = br.readLine()) != null) {
				String[] data = line.split(cvsSplitBy, -1);

				for (int i = 0; i < data.length; i++) {
					data[i] = data[i].replace("\"", "").trim();
				}

				boolean isPrimaryRecord = data.length > 5
										  && data[3] != null && !data[3].isEmpty()
										  && data[5] != null && !data[5].isEmpty();

				if (isPrimaryRecord) {
                    String originalEmpNo = data.length > 0 ? data[0] : "";
                    String firstName = data.length > 3 ? data[3] : "";
                    String lastName = data.length > 5 ? data[5] : "";

                    Optional<Employee> existingEmployee;
                    if (!originalEmpNo.isEmpty()) {
                        existingEmployee = employeeRepository.findByEmpNo(originalEmpNo);
                    } else {
                        List<Employee> existingEmployees = employeeRepository.findByFirstNameAndLastName(firstName, lastName);
                        if (!existingEmployees.isEmpty()) {
                            if (existingEmployees.size() > 1) {
                                log.warn("Warning: Found multiple employees with name: " + firstName + " " + lastName + ". Using the first one found.");
                            }
                            existingEmployee = Optional.of(existingEmployees.get(0));
                        } else {
                            existingEmployee = Optional.empty();
                        }
                    }
                    
                    Employee employee = existingEmployee.orElse(new Employee());

                    if (!existingEmployee.isPresent()) {
                        employee.setFamilyBgList(new ArrayList<>());
                    }

                    String empNo = originalEmpNo;
                    if (empNo.isEmpty()) {
                        empNo = "no-emp-no-" + noEmpNoCounter++;
                    }
                    String plantillaNo = data.length > 1 ? data[1] : "";
                    if (plantillaNo.isEmpty()) {
                        plantillaNo = "no-plantilla-no-" + noPlantillaCounter++;
                    }

					employee.setEmpNo(empNo);
					employee.setPlantillaNo(plantillaNo);
					employee.setPrefix(data.length > 2 ? data[2] : null);
					employee.setFirstName(firstName);
					employee.setMiddleName(data.length > 4 ? data[4] : null);
					employee.setLastName(lastName);
					employee.setSuffix(data.length > 6 ? data[6] : null);
					employee.setBirthPlace(data.length > 8 ? data[8] : null);
					
                    String gender = data.length > 9 ? data[9] : null;
                    if (gender == null || gender.trim().isEmpty() || gender.trim().equalsIgnoreCase("null")) {
                        employee.setGender("L"); 
                    } else {
                        employee.setGender(gender.trim());
                    }

					employee.setCitizenship(data.length > 15 ? data[15] : null);
					employee.setCountryOfOrigin(data.length > 16 ? data[16] : null);
					employee.setCivilStatus(data.length > 17 ? data[17] : null);
					employee.setHeight(data.length > 18 ? data[18] : null);
					employee.setWeight(data.length > 19 ? data[19] : null);
					employee.setBloodType(data.length > 20 ? data[20] : null);
					employee.setGsisIdNo(data.length > 21 ? data[21] : null);
					employee.setPagibigNo(data.length > 22 ? data[22] : null);
					employee.setPhilhealthNo(data.length > 23 ? data[23] : null);
					employee.setSssNo(data.length > 24 ? data[24] : null);
					employee.setTin(data.length > 25 ? data[25] : null);
					employee.setTelNo(data.length > 26 ? data[26] : null);
					employee.setMobileNo1(data.length > 27 ? data[27] : null);
					
                    String email = data.length > 28 ? data[28] : null;
                    if (email != null && !email.isEmpty()) {
                        String sanitizedEmail = email.replaceAll("\\s+", "");
                        if (sanitizedEmail.equalsIgnoreCase("N/A") || !sanitizedEmail.contains("@")) {
                            employee.setEmail1(null);
                        } else {
                            employee.setEmail1(sanitizedEmail);
                        }
                    } else {
                        employee.setEmail1(null);
                    }

					employee.setHouseno1(data.length > 29 ? data[29] : null);
					employee.setStreet1(data.length > 30 ? data[30] : null);
					employee.setSubdivision1(data.length > 31 ? data[31] : null);
					employee.setBrgy1(data.length > 32 ? data[32] : null);
					employee.setCity1(data.length > 33 ? data[33] : null);
					employee.setProvince1(data.length > 34 ? data[34] : null);
					employee.setZipcode1(data.length > 35 ? data[35] : null);
					
                    LocalDate birthdate = data.length > 7 ? parseDate(data[7]) : null;
                    employee.setBirthdate(birthdate);
                    
                    if (!existingEmployee.isPresent()) {
                        employee.setStatus("ACTIVE");
                        employee.setPassword("12345678");
                        employee.setUserType("ROLE_EMPLOYEE");
                        
                        String baseUsername;
                        String lastNameForUsername = employee.getLastName().replaceAll("\\s+", "").toLowerCase();
                        
                        if (birthdate != null) {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMddyyyy");
                            String birthdatePart = birthdate.format(formatter);
                            baseUsername = lastNameForUsername + birthdatePart;
                        } else {
                            log.warn("Warning: Employee " + lastName + ", " + firstName + " is missing a birthdate. Using first name for username instead.");
                            String firstNamePart = employee.getFirstName().replaceAll("\\s+", "").toLowerCase();
                            baseUsername = lastNameForUsername + firstNamePart;
                        }

                        if (employeeRepository.findByUsername(baseUsername).isPresent()) {
                            String uniqueUsername = baseUsername + "-" + empNo;
                            log.warn("Duplicate username '" + baseUsername + "' detected. Generating a unique username: '" + uniqueUsername + "'");
                            employee.setUsername(uniqueUsername);
                        } else {
                            employee.setUsername(baseUsername);
                        }
                    }

					employee.setAssumptiondate(data.length > 14 ? parseDate(data[14]) : null);

                    employee.setDivision(data.length > 10 ? getOrCreateDivision(data[10]) : null);
                    employee.setDistrict(data.length > 11 ? getOrCreateDistrict(data[11]) : null);
                    employee.setEmployeeStatus(data.length > 12 ? getOrCreateEmployeeStatus(data[12]) : null);
                    employee.setPositionTitle(data.length > 13 ? getOrCreatePositionTitle(data[13]) : null);

                    employee.setEmpHashCode(generateAlphanumericHash());
                    
                    log.info(existingEmployee.isPresent() ? "Updating: " : "Saving: " + employee.toString());
					lastSavedEmployee = employeeRepository.save(employee);
					

				} else {
					if (lastSavedEmployee != null && data.length > 14) {
						boolean updated = false;

						if(data.length > 12 && data[12] != null && !data[12].isEmpty()) {
							EmployeeStatus newStatus = getOrCreateEmployeeStatus(data[12]);
                            if(newStatus != null && !newStatus.equals(lastSavedEmployee.getEmployeeStatus())) {
								lastSavedEmployee.setEmployeeStatus(newStatus);
								updated = true;
							}
						}

						if(data.length > 14 && data[14] != null && !data[14].isEmpty()) {
							LocalDate newAssumptionDate = parseDate(data[14]);
							if (newAssumptionDate != null && !newAssumptionDate.equals(lastSavedEmployee.getAssumptiondate())) {
								lastSavedEmployee.setAssumptiondate(newAssumptionDate);
								updated = true;
							}
						}

						if (updated) {
							lastSavedEmployee = employeeRepository.save(lastSavedEmployee);
							log.info("UPDATED: " + lastSavedEmployee.getLastName() + ", " + lastSavedEmployee.getFirstName() + " with new status/date.");
						}
					}
				}
			}

		} catch (IOException e) {
			log.error("Migration IO error", e);
		}

		log.info("End migrateEmployeeInfo.......");
	}
    
    private void countFamilyBgRecords(String filePath) {
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        int count = 0;
        
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);
                // Based on family-bg.csv, firstName is at index 3 and lastName is at index 5
                boolean hasName = data.length > 5
                                  && data[3] != null && !data[3].replace("\"", "").trim().isEmpty()
                                  && data[5] != null && !data[5].replace("\"", "").trim().isEmpty();
                
                if (hasName) {
                    count++;
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }
        log.info("==========================================================");
        log.info("Total family-bg rows with first_name and last_name: " + count);
        log.info("==========================================================");
    }
    
    @Transactional
	public void migrateFamilyBg() {
		log.info("Start migrateFamilyBg.......");

		String csvFile = migrationCsvDir + "/family-bg.csv";
        
        countFamilyBgRecords(csvFile);

		String line = "";
		String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

		try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

			br.readLine(); // Skip header line

			Employee currentEmployee = null;

			while ((line = br.readLine()) != null) {
				String[] data = line.split(cvsSplitBy, -1);

				for (int i = 0; i < data.length; i++) {
					data[i] = data[i].replace("\"", "").trim();
				}

				if (data.length > 0 && data[0] != null && !data[0].isEmpty()) {
					String empNo = data[0];
					Optional<Employee> employeeOpt = employeeRepository.findByEmpNo(empNo);
					if (employeeOpt.isPresent()) {
						currentEmployee = employeeOpt.get();
					} else {
						log.warn("Warning: Employee with empNo " + empNo + " not found. Skipping family background records for this number.");
						currentEmployee = null;
					}
				}

				if (currentEmployee != null) {
					FamilyBg familyMember = new FamilyBg();

                    // 0:empNo, 1:plantillaNo, 2:relationship, 3:firstName, 4:middleName, 5:lastName, 
                    // 6:gender, 7:suffix, 8:occupation, 9:employer, 10:businessAdd, 11:telNo, 12:birthdate
                    
                    familyMember.setEmployee(currentEmployee);
                    familyMember.setRelationship(data.length > 2 ? data[2] : null);
                    familyMember.setFirstName(data.length > 3 ? data[3] : null);
                    familyMember.setMiddleName(data.length > 4 ? data[4] : null);
                    familyMember.setLastName(data.length > 5 ? data[5] : null);
                    familyMember.setSuffix(data.length > 7 ? data[7] : null);
                    familyMember.setEmployer(data.length > 9 ? data[9] : null);
                    familyMember.setBusinessAdd(data.length > 10 ? data[10] : null);
                    familyMember.setTelNo(data.length > 11 ? data[11] : null);
                    familyMember.setBirthdate(data.length > 12 ? parseDate(data[12]) : null);
                    
                    String gender = data.length > 6 ? data[6] : null;
                    if (gender == null || gender.trim().isEmpty() || gender.trim().equalsIgnoreCase("null")) {
                        familyMember.setGender("L");
                    } else {
                        familyMember.setGender(gender.trim());
                    }

                    String occupation = data.length > 8 ? data[8] : null;
                    if (occupation != null && occupation.equalsIgnoreCase("DECEASED")) {
                        familyMember.setDeceased(true);
                        familyMember.setOccupation(null);
                    } else {
                        familyMember.setDeceased(false);
                        familyMember.setOccupation(occupation);
                    }

                    if (familyMember.getFirstName() == null || familyMember.getFirstName().isEmpty() ||
                        familyMember.getLastName() == null || familyMember.getLastName().isEmpty()) {
                        log.warn("Skipping family member for employee " + currentEmployee.getEmpNo() + " due to missing name.");
                    } else {
                        familyBgRepository.save(familyMember);
                        log.info("Saved family member for " + currentEmployee.getEmpNo() + ": " + familyMember.getRelationship());
                    }
				}
			}

		} catch (IOException e) {
			log.error("Migration IO error", e);
		}

		log.info("End migrateFamilyBg.......");
	}
    
    
    @Transactional
    public void migrateEducationalBg() {
        log.info("Start migrateEducationalBg.......");

        String csvFile = migrationCsvDir + "/education-bg.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";
        
        int empNoNotFoundCount = 0;
        int plantillaNoNotFoundCount = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && data[0] != null && !data[0].isEmpty();
                boolean hasPlantillaNo = data.length > 1 && data[1] != null && !data[1].isEmpty();

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    
                    if (hasEmpNo) {
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                        if (!employeeOpt.isPresent()) {
                            empNoNotFoundCount++;
                            log.warn("Warning: Employee with empNo " + data[0] + " not found.");
                        }
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                        if (!employeeOpt.isPresent()) {
                            plantillaNoNotFoundCount++;
                            log.warn("Warning: Employee with plantillaNo " + data[1] + " not found.");
                        }
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    EducationalBackground eduBg = new EducationalBackground();

                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:School, 3:Degree Course, 4:Level, 
                    // 5:Period From, 6:Period To, 7:Highest Level, 8:Year Graduated
                    
                    String schoolName = data.length > 2 ? data[2] : null;
                    String degreeCourseName = data.length > 3 ? data[3] : null;
                    String levelName = data.length > 4 ? data[4] : null;

                    // *** NEW LOGIC: Assign default values if the original value is blank ***
                    if (schoolName == null || schoolName.isEmpty()) {
                        schoolName = "NO SCHOOL";
                    }
                    if (degreeCourseName == null || degreeCourseName.isEmpty()) {
                        degreeCourseName = "NO DEGREE/COURSE";
                    }
                    if (levelName == null || levelName.isEmpty()) {
                        levelName = "NO LEVEL";
                    }

                    eduBg.setEmployee(currentEmployee);
                    
                    eduBg.setSchool(getOrCreateSchool(schoolName));
                    eduBg.setDegreeCourse(getOrCreateDegreeCourse(degreeCourseName));
                    eduBg.setDegreeLevel(getOrCreateDegreeLevel(levelName));

                    eduBg.setStartDate(data.length > 5 ? parseYearToDate(data[5]) : null);
                    eduBg.setEndDate(data.length > 6 ? parseYearToDate(data[6]) : null);
                    eduBg.setUnitsEarned(data.length > 7 ? data[7] : null);
                    eduBg.setYearGraduated(data.length > 8 ? parseIntSafe(data[8]) : 0);
                    
                    educationalBackgroundRepository.save(eduBg);
                    log.info("Saved educational record for " + currentEmployee.getEmpNo() + ": " + schoolName);
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("==========================================================");
        log.info("Total unique empNo values not found: " + empNoNotFoundCount);
        log.info("Total unique plantillaNo values not found (as fallback): " + plantillaNoNotFoundCount);
        log.info("==========================================================");
        log.info("End migrateEducationalBg.......");
    }
    
    @Transactional
    public void migrateWorkExperience() {
        log.info("Start migrateWorkExperience.......");

        String csvFile = migrationCsvDir + "/workexperience.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && !isNullOrNA(data[0]);
                boolean hasPlantillaNo = data.length > 1 && !isNullOrNA(data[1]);

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    String identifier = "";

                    if (hasEmpNo) {
                        identifier = "empNo " + data[0];
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        identifier = "plantillaNo " + data[1];
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        log.warn("Warning: Employee with " + identifier + " not found. Skipping work experience records.");
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:Position, 3:Date From, 4:Date To, 5:Company, 6:Salary, 7:Status, 8:Govt Service
                    
                    // NEW RULE: Check if all relevant columns are blank or N/A
                    boolean isRowEmpty = isNullOrNA(data.length > 3 ? data[3] : null) &&
                                         isNullOrNA(data.length > 4 ? data[4] : null) &&
                                         isNullOrNA(data.length > 2 ? data[2] : null) && // Position Title
                                         isNullOrNA(data.length > 5 ? data[5] : null) && // Company
                                         isNullOrNA(data.length > 6 ? data[6] : null) && // Salary
                                         isNullOrNA(data.length > 7 ? data[7] : null) && // Status
                                         isNullOrNA(data.length > 8 ? data[8] : null);   // Govt Service
                    
                    if (isRowEmpty) {
                        log.info("Skipping empty work experience row for employee " + currentEmployee.getEmpNo());
                        continue;
                    }

                    WorkExperience workExperience = new WorkExperience();
                    workExperience.setEmployee(currentEmployee);
                    
                    workExperience.setDateFrom(data.length > 3 ? parseDate(data[2]) : null);
                    workExperience.setDateTo(data.length > 4 ? parseDate(data[3]) : null);
                    workExperience.setPositionTitle(data.length > 2 ? data[4] : null);
                    workExperience.setDepartment(data.length > 5 ? data[5] : null); // Mapping Company to department
                    workExperience.setSalary(data.length > 6 ? parseBigDecimalSafe(data[6]) : null);
                    
                    String gradeStr = data.length > 7 ? data[7] : "";
                    int salaryGrade;

                    try {
                        salaryGrade = Integer.parseInt(gradeStr.trim());
                    } catch (NumberFormatException e) {
                        salaryGrade = 0;
                    }

                    workExperience.setSalaryGrade(salaryGrade);
                    
                    
                    workExperience.setAppointmentStatus(data.length > 8 ? data[8] : null);
                    workExperience.setGovtOffice(data.length > 9 ? data[9] : null);
                    
                    workExperienceRepository.save(workExperience);
                    log.info("Saved work experience for " + currentEmployee.getEmpNo() + ": " + workExperience.getPositionTitle());
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("End migrateWorkExperience.......");
    }
    
    
    @Transactional
    public void migrateLearning() {
        log.info("Start migrateLearning.......");

        String csvFile = migrationCsvDir + "/learning.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && !isNullOrNA(data[0]);
                boolean hasPlantillaNo = data.length > 1 && !isNullOrNA(data[1]);

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    String identifier = "";

                    if (hasEmpNo) {
                        identifier = "empNo " + data[0];
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        identifier = "plantillaNo " + data[1];
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        log.warn("Warning: Employee with " + identifier + " not found. Skipping learning records.");
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:Title, 3:Date From, 4:Date To, 5:No. of Hours, 6:Type, 7:Conducted By
                    
                    // NEW RULE: Check if all relevant columns are blank or N/A
                    boolean isRowEmpty = isNullOrNA(data.length > 2 ? data[2] : null) && // Title
                                         isNullOrNA(data.length > 3 ? data[3] : null) && // Date From
                                         isNullOrNA(data.length > 4 ? data[4] : null) && // Date To
                                         isNullOrNA(data.length > 5 ? data[5] : null) && // No. of Hours
                                         isNullOrNA(data.length > 6 ? data[6] : null) && // Type
                                         isNullOrNA(data.length > 7 ? data[7] : null);   // Conducted By
                    
                    if (isRowEmpty) {
                        log.info("Skipping empty learning row for employee " + currentEmployee.getEmpNo());
                        continue;
                    }

                    LearningAndDevelopment learning = new LearningAndDevelopment();
                    learning.setEmployee(currentEmployee);
                    learning.setTitleOfSeminar(data.length > 2 ? data[2] : null);
                    learning.setDateFrom(data.length > 3 ? parseDate(data[3]) : null);
                    learning.setDateTo(data.length > 4 ? parseDate(data[4]) : null);
                    
                    // Safely parse integer for noHours
                    if (data.length > 5 && !isNullOrNA(data[5])) {
                        learning.setNoHours(parseIntSafe(data[5]));
                    }

                    learning.setLearningType(data.length > 6 ? data[6] : null);
                    learning.setProviders(data.length > 7 ? data[7] : null);
                    
                    learningAndDevelopmentRepository.save(learning);
                    log.info("Saved learning record for " + currentEmployee.getEmpNo() + ": " + learning.getTitleOfSeminar());
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("End migrateLearning.......");
    }
    
    
    @Transactional
    public void migrateOtherInfo() {
        log.info("Start migrateOtherInfo.......");

        String csvFile = migrationCsvDir + "/otherinfo.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && !isNullOrNA(data[0]);
                boolean hasPlantillaNo = data.length > 1 && !isNullOrNA(data[1]);

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    String identifier = "";

                    if (hasEmpNo) {
                        identifier = "empNo " + data[0];
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        identifier = "plantillaNo " + data[1];
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        log.warn("Warning: Employee with " + identifier + " not found. Skipping other info records.");
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:Special Skills, 3:Non Academic Distinction, 4:Membership in Association
                    
                    String specialSkill = data.length > 2 ? data[2] : null;
                    String nonAcademic = data.length > 3 ? data[3] : null;
                    String membership = data.length > 4 ? data[4] : null;

                    // NEW RULE: Check if all relevant columns are blank or N/A
                    boolean isRowEmpty = isNullOrNA(specialSkill) &&
                                         isNullOrNA(nonAcademic) &&
                                         isNullOrNA(membership);
                    
                    if (isRowEmpty) {
                        log.info("Skipping empty other info row for employee " + currentEmployee.getEmpNo());
                        continue;
                    }

                    OtherInfo otherInfo = new OtherInfo();
                    otherInfo.setEmployee(currentEmployee);
                    otherInfo.setSpecialSkill(specialSkill);
                    otherInfo.setNonAcademic(nonAcademic);
                    otherInfo.setMembershipInAssociation(membership);
                    
                    otherInfoRepository.save(otherInfo);
                    log.info("Saved other info record for " + currentEmployee.getEmpNo());
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("End migrateOtherInfo.......");
    }
    
    
    @Transactional
    public void migrateGovId() {
        log.info("Start migrateGovId.......");

        String csvFile = migrationCsvDir + "/govid.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && !isNullOrNA(data[0]);
                boolean hasPlantillaNo = data.length > 1 && !isNullOrNA(data[1]);

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    String identifier = "";

                    if (hasEmpNo) {
                        identifier = "empNo " + data[0];
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        identifier = "plantillaNo " + data[1];
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        log.warn("Warning: Employee with " + identifier + " not found. Skipping government ID records.");
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:Government Issued Id, 3:ID/License/Passport No., 4:Date of issuance, 5:Place of Issuance
                    
                    String idName = data.length > 2 ? data[2] : null;
                    String idNo = data.length > 3 ? data[3] : null;
                    String dateOfIssuance = data.length > 4 ? data[4] : null;
                    String placeOfIssuance = data.length > 5 ? data[5] : null;

                    // NEW RULE: Check if all relevant columns are blank or N/A
                    boolean isRowEmpty = isNullOrNA(idName) &&
                                         isNullOrNA(idNo) &&
                                         isNullOrNA(dateOfIssuance) &&
                                         isNullOrNA(placeOfIssuance);
                    
                    if (isRowEmpty) {
                        log.info("Skipping empty government ID row for employee " + currentEmployee.getEmpNo());
                        continue;
                    }

                    GovermentIssuedId govId = new GovermentIssuedId();
                    govId.setEmployee(currentEmployee);
                    govId.setGovermentIssuedName(idName);
                    govId.setIdNo(idNo);
                    govId.setIssuanceDate(parseDate(dateOfIssuance));
                    govId.setPlaceOfIssuance(placeOfIssuance);
                    
                    govermentIssuedIdRepository.save(govId);
                    log.info("Saved government ID record for " + currentEmployee.getEmpNo() + ": " + govId.getGovermentIssuedName());
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("End migrateGovId.......");
    }
    
    @Transactional
    public void migrateReference() {
        log.info("Start migrateReference.......");

        String csvFile = migrationCsvDir + "/reference.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && !isNullOrNA(data[0]);
                boolean hasPlantillaNo = data.length > 1 && !isNullOrNA(data[1]);

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    String identifier = "";

                    if (hasEmpNo) {
                        identifier = "empNo " + data[0];
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        identifier = "plantillaNo " + data[1];
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        log.warn("Warning: Employee with " + identifier + " not found. Skipping reference records.");
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:Reference Name, 3:Position, 4:Address, 5:Tel No.
                    
                    String referenceName = data.length > 2 ? data[2] : null;
                    String position = data.length > 3 ? data[3] : null;
                    String address = data.length > 4 ? data[4] : null;
                    String telNo = data.length > 5 ? data[5] : null;

                    // NEW RULE: Check if all relevant columns are blank or N/A
                    boolean isRowEmpty = isNullOrNA(referenceName) &&
                                         isNullOrNA(position) &&
                                         isNullOrNA(address) &&
                                         isNullOrNA(telNo);
                    
                    if (isRowEmpty) {
                        log.info("Skipping empty reference row for employee " + currentEmployee.getEmpNo());
                        continue;
                    }

                    EmpReferences reference = new EmpReferences();
                    reference.setEmployee(currentEmployee);
                    reference.setReferenceName(referenceName);
                    reference.setPositionTitle(position);
                    reference.setCompanyAddress(address);
                    reference.setCompanyContactNo(telNo);
                    
                    empReferencesRepository.save(reference);
                    log.info("Saved reference record for " + currentEmployee.getEmpNo() + ": " + reference.getReferenceName());
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("End migrateReference.......");
    }
    
    @Transactional
    public void migrateServiceRecord() {
        log.info("Start migrateServiceRecord.......");

        String csvFile = migrationCsvDir + "/servicerecord.csv";
        String line = "";
        String cvsSplitBy = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            br.readLine(); // Skip header line

            Employee currentEmployee = null;

            while ((line = br.readLine()) != null) {
                String[] data = line.split(cvsSplitBy, -1);

                for (int i = 0; i < data.length; i++) {
                    data[i] = data[i].replace("\"", "").trim();
                }

                boolean hasEmpNo = data.length > 0 && !isNullOrNA(data[0]);
                boolean hasPlantillaNo = data.length > 1 && !isNullOrNA(data[1]);

                if (hasEmpNo || hasPlantillaNo) {
                    Optional<Employee> employeeOpt = Optional.empty();
                    String identifier = "";

                    if (hasEmpNo) {
                        identifier = "empNo " + data[0];
                        employeeOpt = employeeRepository.findByEmpNo(data[0]);
                    }
                    
                    if (!employeeOpt.isPresent() && hasPlantillaNo) {
                        identifier = "plantillaNo " + data[1];
                        employeeOpt = employeeRepository.findByPlantillaNo(data[1]);
                    }

                    if (employeeOpt.isPresent()) {
                        currentEmployee = employeeOpt.get();
                    } else {
                        log.warn("Warning: Employee with " + identifier + " not found. Skipping service records.");
                        currentEmployee = null;
                    }
                }

                if (currentEmployee != null) {
                    // CSV Columns: 0:empNo, 1:plantillaNo, 2:signingDate, 3:dateFrom, 4:dateTo, 5:designation, 6:branch, 
                    // 7:Employment Status, 8:Position Title, 9:salary, 10:station, 11:lvAbs, 12:separationCause, 
                    // 13:separationDate, 14:vice, 15:statusOfSepeparation, 16:statusOfAppointment, 17:salaryGrade, 18:stepInc, 19:district

                    // NEW RULE: Check if all relevant columns are blank or N/A
                    boolean isRowEmpty = true;
                    for (int i = 2; i <= 19; i++) {
                        if (data.length > i && !isNullOrNA(data[i])) {
                            isRowEmpty = false;
                            break;
                        }
                    }
                    
                    if (isRowEmpty) {
                        log.info("Skipping empty service record row for employee " + currentEmployee.getEmpNo());
                        continue;
                    }

                    ServiceRecord serviceRecord = new ServiceRecord();
                    serviceRecord.setEmployee(currentEmployee);
                    serviceRecord.setSigningDate(data.length > 2 ? parseDate(data[2]) : null);
                    serviceRecord.setDateFrom(data.length > 3 ? parseDate(data[3]) : null);
                    serviceRecord.setDateTo(data.length > 4 ? parseDate(data[4]) : null);
                    serviceRecord.setDesignation(data.length > 5 ? data[5] : null);
                    serviceRecord.setBranch(data.length > 6 ? data[6] : null);
                    
                    // Get or Create for related entities
                    String statusName = data.length > 7 ? data[7] : null;
                    serviceRecord.setEmployeeStatus(getOrCreateEmployeeStatus(statusName));
                    
                    String titleName = data.length > 8 ? data[8] : null;
                    serviceRecord.setPositionTitle(getOrCreatePositionTitle(titleName));

                    serviceRecord.setSalary(data.length > 9 ? parseDoubleSafe(data[9]) : null);
                    serviceRecord.setStation(data.length > 10 ? data[10] : null);
                    serviceRecord.setLvAbs(data.length > 11 ? data[11] : null);
                    serviceRecord.setSeparationCause(data.length > 12 ? data[12] : null);
                    serviceRecord.setSeparationDate(data.length > 13 ? parseDate(data[13]) : null);
                    serviceRecord.setVice(data.length > 14 ? data[14] : null);
                    serviceRecord.setStatusOfSepeparation(data.length > 15 ? data[15] : null);
                    serviceRecord.setStatusOfAppointment(data.length > 16 ? data[16] : null);
                    
                    if (data.length > 17 && !isNullOrNA(data[17])) {
                        serviceRecord.setSalaryGrade(parseIntSafe(data[17]));
                    }
                    if (data.length > 18 && !isNullOrNA(data[18])) {
                        serviceRecord.setStepInc(parseIntSafe(data[18]));
                    }
                    
                    serviceRecord.setDistrict(data.length > 19 ? data[19] : null);
                    
                    serviceRecordRepository.save(serviceRecord);
                    log.info("Saved service record for " + currentEmployee.getEmpNo() + ": " + serviceRecord.getDesignation());
                }
            }
        } catch (IOException e) {
            log.error("Migration IO error", e);
        }

        log.info("End migrateServiceRecord.......");
    }
    
    public static String generateAlphanumericHash() {
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            char c = uuid.charAt(i);
            if (Character.isDigit(c) || Character.isLetter(c)) {
                sb.append(c);
            } else {
                sb.append((char) ('0' + (c % 10)));
            }
        }
        return sb.toString();
    }
    
    
}
