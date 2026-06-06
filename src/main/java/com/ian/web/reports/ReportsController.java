package com.ian.web.reports;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.approvers.ClearanceApprovers;
import com.ian.web.employee.approvers.ClearanceApproversRepository;
import com.ian.web.employee.approvers.ServiceRecordSignatory;
import com.ian.web.employee.approvers.ServiceRecordSignatoryRepository;
import com.ian.web.employee.clearance.Clearance;
import com.ian.web.employee.clearance.ClearanceRepository;
import com.ian.web.employee.educationalbg.EducationalBackground;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibility;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBg;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedId;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopment;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfo;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestion;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferences;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.servicerecord.ServiceRecord;
import com.ian.web.employee.servicerecord.ServiceRecordReportDto;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequest;
import com.ian.web.employee.servicerecord.ServiceRecordReportRequestRepository;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWork;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperience;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.fileupload.FileStorageProperties;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRPrintPage;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.JasperRunManager;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

@Controller
@RequiredArgsConstructor
public class ReportsController {
	
	public static final String HEADER_REPORT_NAME = "Sangguniang Panlungsod ng Maynila";
	public static final String EMR_RX_LOGO_URL = "/resources/static/images/rx.jpg";
	
	private final EmployeeRepository employeeRepository;	
	private final PositionTitleRepository positionTitleRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final DivisionRepository divisionRepository;
	
	private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
	private final ClearanceRepository clearanceRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	private final ClearanceApproversRepository clearanceApproversRepository;
	private final ServiceRecordReportRequestRepository serviceRecordReportRequestRepository;
	private final ServiceRecordSignatoryRepository serviceRecordSignatoryRepository;
	private final FileStorageProperties fileStorageProperties;

	private final ResourceLoader resourceLoader;
	
	@GetMapping("/viewPds/{employeeId}")
	public void viewPdsNew(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<FamilyBg> fbList = familyBgRepository.findByEmployeeId(employeeId);
		List<EducationalBackground> eduList = educationalBackgroundRepository.findByEmployeeId(employeeId);
		List<CivilServiceEligibility> csList = civilServiceEligibilityRepository.findByEmployeeId(employeeId);
		List<WorkExperience> workExList = workExperienceRepository.findByEmployeeId(employeeId);
		List<VoluntaryWork> voluntaryList = voluntaryWorkRepository.findByEmployeeId(employeeId);
		List<LearningAndDevelopment> learningList = learningAndDevelopmentRepository.findByEmployeeId(employeeId);
		List<OtherInfo> otherInfoList = otherInfoRepository.findByEmployeeId(employeeId);		
		List<OtherInfoQuestion> otherInfoQuestionList = otherInfoQuestionRepository.findByEmployeeId(employeeId);
		List<EmpReferences> refList = empReferencesRepository.findByEmployeeId(employeeId);
		List<GovermentIssuedId> govList = govermentIssuedIdRepository.findByEmployeeId(employeeId);
		
		OtherInfoQuestion otherObj = new OtherInfoQuestion();
		if(!otherInfoQuestionList.isEmpty()) {
			otherObj = otherInfoQuestionList.get(0);
		}
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		ServiceRecordReportDto x = new ServiceRecordReportDto();
		x.setStation("test");
		
		reportList.add(x);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");

		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2025_P1.jasper");
		Map<String, Object> map = populateMapReport2025_P1(employee, fbList, eduList);
		
		InputStream reportStream2 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2025_P2.jasper");
		Map<String, Object> map2 = populateMapReport2025_P2(csList, workExList);
		
		InputStream reportStream3 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2025_P3.jasper");
		Map<String, Object> map3 = populateMapReport2025_P3(voluntaryList, learningList, otherInfoList);
		
		InputStream reportStream4 = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2025_P4.jasper");
		Map<String, Object> map4 = populateMapReport2025_P4(otherObj, refList, govList);

		// Add employee photo to P4 if available
		InputStream photoStream = null;
		String profilePhoto = employee.getProfilePhoto();
		if (profilePhoto != null && !profilePhoto.isEmpty()) {
			String fileName = profilePhoto.substring(profilePhoto.lastIndexOf('/') + 1);
			Path photoPath = Paths.get(fileStorageProperties.getUploadDir()).resolve(fileName);
			if (Files.exists(photoPath)) {
				try {
					photoStream = Files.newInputStream(photoPath);
					map4.put("VIII.EmployeePhoto", photoStream);
				} catch (Exception e) {
					// If photo load fails, continue without it
					map4.put("VIII.EmployeePhoto", null);
				}
			} else {
				map4.put("VIII.EmployeePhoto", null);
			}
		} else {
			map4.put("VIII.EmployeePhoto", null);
		}

		/////////

		JasperPrint jasperPrint1 = JasperFillManager.fillReport(reportStream, map, new JREmptyDataSource());
		JasperPrint jasperPrint2 = JasperFillManager.fillReport(reportStream2, map2, new JREmptyDataSource());
		JasperPrint jasperPrint3 = JasperFillManager.fillReport(reportStream3, map3, new JREmptyDataSource());
		JasperPrint jasperPrint4 = JasperFillManager.fillReport(reportStream4, map4, new JREmptyDataSource());
		
		System.out.println("Number of pages in report 1: " + jasperPrint1.getPages().size());
		System.out.println("Number of pages in report 2: " + jasperPrint2.getPages().size());
		System.out.println("Number of pages in report 3: " + jasperPrint3.getPages().size());
		System.out.println("Number of pages in report 4: " + jasperPrint4.getPages().size());

		
		List<JRPrintPage> pages2 = jasperPrint2.getPages();
        for (JRPrintPage page : pages2) {
            jasperPrint1.addPage(page);
        }
        
        List<JRPrintPage> pages3 = jasperPrint3.getPages();
        for (JRPrintPage page : pages3) {
            jasperPrint1.addPage(page);
        }
        
        List<JRPrintPage> pages4 = jasperPrint4.getPages();
        for (JRPrintPage page : pages4) {
            jasperPrint1.addPage(page);
        }

        JasperExportManager.exportReportToPdfStream(jasperPrint1, response.getOutputStream());

	}
	
	
	@GetMapping("/viewPdsxxxx/{employeeId}")	
	public void viewPds(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<FamilyBg> fbList = familyBgRepository.findByEmployeeId(employeeId);
		List<EducationalBackground> eduList = educationalBackgroundRepository.findByEmployeeId(employeeId);
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		ServiceRecordReportDto x = new ServiceRecordReportDto();
		x.setStation("test");
		
		reportList.add(x);
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");

		InputStream pdsFullStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jasper/reports/PDSFULL.jasper");
		InputStream pds1Stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS1.jasper");
		InputStream pds2Stream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/PDS2.jasper");
		
		JasperReport pdsFullReport = (JasperReport) JRLoader.loadObject(pdsFullStream);
		JasperReport pds1Report = (JasperReport) JRLoader.loadObject(pds1Stream);
		JasperReport pds2Report = (JasperReport) JRLoader.loadObject(pds2Stream);
		
		
		
		if(pds1Stream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		if (pds1Stream == null || pds2Stream == null) {
            System.err.println("One or both reports could not be loaded.");
            return;
        }
		
		Map<String, Object> map = populateMapReport1(employee, fbList, eduList);
		
		File file2 = ResourceUtils.getFile("classpath:static/images/PDS2.png");
		String bgImg2 = file2.getAbsolutePath();
		map.put("V.CSE_Career_Service_RA_1080_1", "test");
		map.put("FormBg2", bgImg2);
		
		map.put("PDS1", pds1Report);
		map.put("PDS2", pds2Report);
		
		SequenceInputStream mergedStream = new SequenceInputStream(pds1Stream, pds2Stream);
		
		JasperPrint pdsFullPrint = JasperFillManager.fillReport(pdsFullReport, map, new JREmptyDataSource());
		
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(pdsFullPrint, outputStream);
        
        // Write the PDF stream to output or do whatever you need with it
        response.getOutputStream().write(outputStream.toByteArray());
        
//		JasperPrint pds1Print = JasperFillManager.fillReport(pds1Report, map, beanColDataSource);
//		JasperPrint pds2Print = JasperFillManager.fillReport(pds2Report, map, beanColDataSource);
//		
//		JasperPrint mergedPrint = new JasperPrint();
//        mergedPrint.addPage(pds1Print.getPages().get(0));
//        mergedPrint.addPage(pds2Print.getPages().get(0));
//        
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        JasperExportManager.exportReportToPdfStream(mergedPrint, outputStream);
//        
//        response.getOutputStream().write(outputStream.toByteArray());
		
//		JasperRunManager.runReportToPdfStream(pds2Stream,	response.getOutputStream(), map, beanColDataSource);
//		JasperRunManager.runReportToPdfStream(pds2Stream,	response.getOutputStream(), map, beanColDataSource);
	}
	
	private Map<String, Object> populateMapReport1(Employee emp, List<FamilyBg> fbList, List<EducationalBackground> eduList) throws FileNotFoundException {
		File file = ResourceUtils.getFile("classpath:static/images/PDS1.png");
		String bgImg = file.getAbsolutePath();
		
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("FormBg", bgImg);
		
		map.put("CS_ID_no.", "");
		map.put("I.PI_Surname", "  " + emp.getLastName());
		map.put("I.PI_Firstname", "  " + emp.getFirstName());
		map.put("I.PI_NameExtension", "  " + emp.getSuffix());
		map.put("I.PI_Middlename", "  " + emp.getMiddleName());
		map.put("I.PI_Date_Of_Birth", "  " + formatDate(emp.getBirthdate()));
		map.put("I.PI_Place_Of_Birth", "  " + emp.getBirthPlace());
		
		if("M".equalsIgnoreCase(emp.getGender())) {
			map.put("I.PI_Sex_M", " X");
			map.put("I.PI_Sex_F", "");
		} else {
			map.put("I.PI_Sex_M", "");
			map.put("I.PI_Sex_F", " X");
		}
		
		if("SINGLE".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", " X");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", "");
		} else if("MARRIED".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", " X");
			map.put("I.PI_Civil_Status_Separated", "");
		} else if("WIDOWED".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", " X");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", "");			
		} else if("SEPARATED".equalsIgnoreCase(emp.getCivilStatus())) {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", "");
			map.put("I.PI_Civil_Status_Others_Text", "");
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", " X");
		} else {
			map.put("I.PI_Civil_Status_Single", "");
			map.put("I.PI_Civil_Status_Widowed", "");
			map.put("I.PI_Civil_Status_Others", " X");
			map.put("I.PI_Civil_Status_Others_Text", ""); //TODO fix this
			map.put("I.PI_Civil_Status_Married", "");
			map.put("I.PI_Civil_Status_Separated", "");
		}
		
		
		map.put("I.PI_Height", "  " + getStringValue(emp.getHeight()));
		map.put("I.PI_Weight", "  " + getStringValue(emp.getWeight()));
		map.put("I.PI_Bloodtype", "  " + getStringValue(emp.getBloodType()));
		map.put("I.PI_GSIS_ID_NO.", "  " + getDisplayValue(emp.getGsisIdNo()));
		map.put("I.PI_Pagibig_ID_NO.", "  " + getDisplayValue(emp.getPagibigNo()));
		map.put("I.PI_PhilHealth_NO.", "  " + getDisplayValue(emp.getPhilhealthNo()));
		map.put("I.PI_SSS_NO.", "  " + getDisplayValue(emp.getSssNo()));
		map.put("I.PI_TIN_NO.", "  " + getDisplayValue(emp.getTin()));
		map.put("I.PI_Agency_Employee_NO.", "  " + getDisplayValue(emp.getEmpNo()));
		
		//Residential Address
		//Get Province Map
		
		
		map.put("PI_Residential_House_No", "  " + getStringValue(emp.getHouseno1()));
		map.put("PI_Residential_Street", "  " + getStringValue(emp.getStreet1()));
		map.put("PI_Residential_Subdivision", "  " + getStringValue(emp.getSubdivision1()));
		map.put("PI_Residential_Barangay", "  " + getStringValue(emp.getBrgy1()));
		map.put("PI_Residential_City", "  " + getStringValue(emp.getCity1()));
		map.put("PI_Residential_Province", "  " + getStringValueProvince(emp.getProvince1()));
		map.put("PI_Residential_Zipcode", "  " + getStringValue(emp.getZipcode1()));
		
		map.put("PI_Permanent_House_No", "  " + getStringValue(emp.getHouseno2()));
		map.put("PI_Permanent_Street", "  " + getStringValue(emp.getStreet2()));
		map.put("PI_Permanent_Subdivision", "  " + getStringValue(emp.getSubdivision2()));
		map.put("PI_Permanent_Barangay", "  " + getStringValue(emp.getBrgy2()));
		map.put("PI_Permanent_City", "  " + getStringValue(emp.getCity2()));
		map.put("PI_Permanent_Province", "  " + getStringValueProvince(emp.getProvince2()));
		map.put("PI_Permanent_Zipcode", "  " + getStringValue(emp.getZipcode2()));
		
		// 16. CITIZENSHIP — Annex H-1 semantics:
		//   Filipino + Dual are mutually exclusive top-level options.
		//   by birth / by naturalization are sub-options under Dual Citizenship only.
		String citizenship = emp.getCitizenship();
		if("FILIPINO BY BIRTH".equalsIgnoreCase(citizenship)
				|| "FILIPINO".equalsIgnoreCase(citizenship)
				|| "FILIPINO BY NATURALIZATION".equalsIgnoreCase(citizenship)) {
			map.put("I.PI_Citizenship_Filipino", " X");
			map.put("I.PI_Citizenship_Dual", "");
			map.put("I.PI_Citizenship_By_birth", "");
			map.put("I.PI_Citizenship_By_naturalization", "");
			map.put("I.PI_Citizenship_Indicate_Country", "");
		} else if("DUAL CITIZENSHIP BY BIRTH".equalsIgnoreCase(citizenship)) {
			map.put("I.PI_Citizenship_Filipino", "");
			map.put("I.PI_Citizenship_Dual", " X");
			map.put("I.PI_Citizenship_By_birth", " X");
			map.put("I.PI_Citizenship_By_naturalization", "");
			map.put("I.PI_Citizenship_Indicate_Country", "  " + getCountryName(emp.getCountryOfOrigin()));
		} else if("DUAL CITIZENSHIP BY NATURALIZATION".equalsIgnoreCase(citizenship)) {
			map.put("I.PI_Citizenship_Filipino", "");
			map.put("I.PI_Citizenship_Dual", " X");
			map.put("I.PI_Citizenship_By_birth", "");
			map.put("I.PI_Citizenship_By_naturalization", " X");
			map.put("I.PI_Citizenship_Indicate_Country", "  " + getCountryName(emp.getCountryOfOrigin()));
		} else {
			map.put("I.PI_Citizenship_Filipino", "");
			map.put("I.PI_Citizenship_Dual", "");
			map.put("I.PI_Citizenship_By_birth", "");
			map.put("I.PI_Citizenship_By_naturalization", "");
			map.put("I.PI_Citizenship_Indicate_Country", "");
		}
		
		
		map.put("I.PI_Telephone_NO", "  " + (emp.getTelNo() != null ? emp.getTelNo() : ""));
		map.put("I.PI_Mobile_NO", "  " + (emp.getMobileNo1() != null ? emp.getMobileNo1() : ""));
		map.put("I.PI_EmailAdd", "  " + (emp.getEmail1() != null ? emp.getEmail1() : ""));
		
		int ctrForChild = 1;
		boolean spousePopulated = false;
		boolean fatherPopulated = false;
		boolean motherPopulated = false;
		
		//Family
		for(FamilyBg fb : fbList) {
			if("SPOUSE".equalsIgnoreCase(fb.getRelationship())) {
				map.put("II.FBG_Spouse_Surname", "  " + fb.getLastName());
				map.put("II.FBG_Spouse_Firstname", "  " + fb.getFirstName());
				map.put("II.FBG_Spouse_Name_Extension", "  " + fb.getSuffix());
				map.put("II.FBG_Spouse_Middlename", "  " + fb.getMiddleName());
				map.put("II.FBG_Spouse_Occupation", "  " + fb.getOccupation());
				map.put("II.FBG_Spouse_Employer", "  " + fb.getEmployer());
				map.put("II.FBG_Spouse_Business_Address", "  " + fb.getBusinessAdd());
				map.put("II.FBG_Spouse_TelephoneNO", "  " + fb.getTelNo());
				spousePopulated = true;
			} else if("FATHER".equalsIgnoreCase(fb.getRelationship())) {
				fatherPopulated = true;
				map.put("II.FBG_Father_Surname", "  " + fb.getLastName());
				map.put("II.FBG_Father_Firstname", "  " + fb.getFirstName());
				map.put("II.FBG_Father_Name_Extension", "  " + fb.getSuffix());
				map.put("II.FBG_Father_Middlename", "  " + fb.getMiddleName());
				map.put("II.FBG_Father_Birthday", "  " + formatDateOrNA(fb.getBirthdate()));
			} else if("MOTHER".equalsIgnoreCase(fb.getRelationship())) {
				map.put("II.FBG_Mother_Maidenname", "  " + getStringValue(fb.getMaidenName()));
				map.put("II.FBG_Mother_Surname", "  " + fb.getLastName());
				map.put("II.FBG_Mother_Firstname", "  " + fb.getFirstName());
				map.put("II.FBG_Mother_Middlename", "  " + fb.getMiddleName());
				map.put("II.FBG_Mother_Birthday", "  " + formatDateOrNA(fb.getBirthdate()));
				motherPopulated = true;
			} else {
				map.put("II.FBG_Child_name"+ctrForChild, "  " + fb.getFirstName() + " " + fb.getMiddleName() + " " + fb.getLastName());
				map.put("II.FBG_Child_Birthday"+ctrForChild, "  " + formatDateOrNA(fb.getBirthdate()));
				ctrForChild++;
			}			
		}
		
		if(motherPopulated) {

		} else {
			map.put("II.FBG_Mother_Maidenname", "");
			map.put("II.FBG_Mother_Surname", "");
			map.put("II.FBG_Mother_Firstname", "");
			map.put("II.FBG_Mother_Middlename", "");
			map.put("II.FBG_Mother_Birthday", "");
		}

		if(fatherPopulated) {

		} else {
			map.put("II.FBG_Father_Surname", "");
			map.put("II.FBG_Father_Firstname", "");
			map.put("II.FBG_Father_Name_Extension", "");
			map.put("II.FBG_Father_Middlename", "");
			map.put("II.FBG_Father_Birthday", "");
		}

		if(spousePopulated) {
			
		} else {
			map.put("II.FBG_Spouse_Surname", "");
			map.put("II.FBG_Spouse_Firstname", "");
			map.put("II.FBG_Spouse_Name_Extension", "");
			map.put("II.FBG_Spouse_Middlename", "");
			map.put("II.FBG_Spouse_Occupation", "");
			map.put("II.FBG_Spouse_Employer", "");
			map.put("II.FBG_Spouse_Business_Address", "");
			map.put("II.FBG_Spouse_TelephoneNO", "");
		}
		
		if(ctrForChild < 12) {
			for(int x = ctrForChild; x <= 12; x++) {
				map.put("II.FBG_Child_name"+x, "");				
				map.put("II.FBG_Child_Birthday"+x, "");
			}
		}		
		
		//Education BG
		boolean elemPopulated = false;
		boolean secPopulated = false;
		boolean vocPopulated = false;
		boolean collegePopulated = false;
		boolean gradopulated = false;
		
		for(EducationalBackground eb : eduList) {
			if("ELEMENTARY".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())
					|| "ELEMENTARY GRADUATE".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				elemPopulated = true;
				map.put("III.EB_Elementary_School", " " + eb.getEffectiveSchoolName());

				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_Elementary_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_Elementary_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_Elementary_BasicEducation_Degree_Course", "");
				}

				map.put("III.EB_Elementary_Period_Of_Attendance_From", " " + formatDateMonthYearOnly(eb.getStartDate()));
				map.put("III.EB_Elementary_Period_Of_Attendance_To", " " + formatDateMonthYearOnly(eb.getEndDate()));
				map.put("III.EB_Elementary_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_Elementary_Year_Graduated", " " + eb.getYearGraduated());

				{
					String honorsVal = "";
					if (eb.getAcademicHonors() != null && eb.getAcademicHonors().getAcademicHonorsName() != null) {
						honorsVal = " " + eb.getAcademicHonors().getAcademicHonorsName();
					} else if (eb.getScholarship() != null && eb.getScholarship().getScholarshipName() != null) {
						honorsVal = " " + eb.getScholarship().getScholarshipName();
					}
					map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", honorsVal);
				}
				
				
			} else if("SECONDARY".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				secPopulated = true;
				map.put("III.EB_Secondary_School", " " + eb.getEffectiveSchoolName());

				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_Secondary_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_Secondary_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_Secondary_BasicEducation_Degree_Course", "");
				}

				map.put("III.EB_Secondary_Period_Of_Attendance_From",  " " + formatDateMonthYearOnly(eb.getStartDate()));
				map.put("III.EB_Secondary_Period_Of_Attendance_To", " " + formatDateMonthYearOnly(eb.getEndDate()));
				map.put("III.EB_Secondary_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_Secondary_Year_Graduated", " " + eb.getYearGraduated());

				{
					String honorsVal = "";
					if (eb.getAcademicHonors() != null && eb.getAcademicHonors().getAcademicHonorsName() != null) {
						honorsVal = " " + eb.getAcademicHonors().getAcademicHonorsName();
					} else if (eb.getScholarship() != null && eb.getScholarship().getScholarshipName() != null) {
						honorsVal = " " + eb.getScholarship().getScholarshipName();
					}
					map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", honorsVal);
				}
			} else if("VOCATIONAL".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				vocPopulated = true;
				map.put("III.EB_Vocational_TradeCourse_School", " " + eb.getEffectiveSchoolName());

				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course", "");
					}
				} else {
					map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course", "");
				}

				map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_From",  " " + formatDateMonthYearOnly(eb.getStartDate()));
				map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_To", " " + formatDateMonthYearOnly(eb.getEndDate()));
				map.put("III.EB_Vocational_TradeCourse_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_Vocational_TradeCourse_Year_Graduated", " " + eb.getYearGraduated());

				{
					String honorsVal = "";
					if (eb.getAcademicHonors() != null && eb.getAcademicHonors().getAcademicHonorsName() != null) {
						honorsVal = " " + eb.getAcademicHonors().getAcademicHonorsName();
					} else if (eb.getScholarship() != null && eb.getScholarship().getScholarshipName() != null) {
						honorsVal = " " + eb.getScholarship().getScholarshipName();
					}
					map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", honorsVal);
				}
			} else if("COLLEGE".equalsIgnoreCase(eb.getDegreeLevel().getDegreeName())) {
				collegePopulated = true;
				map.put("III.EB_College_School", " " + eb.getEffectiveSchoolName());

				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_College_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_College_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_College_BasicEducation_Degree_Course", "");
				}

				map.put("III.EB_College_Period_Of_Attendance_From",  " " + formatDateMonthYearOnly(eb.getStartDate()));
				map.put("III.EB_College_Period_Of_Attendance_To", " " + formatDateMonthYearOnly(eb.getEndDate()));
				map.put("III.EB_College_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_College_Year_Graduated", " " + eb.getYearGraduated());

				{
					String honorsVal = "";
					if (eb.getAcademicHonors() != null && eb.getAcademicHonors().getAcademicHonorsName() != null) {
						honorsVal = " " + eb.getAcademicHonors().getAcademicHonorsName();
					} else if (eb.getScholarship() != null && eb.getScholarship().getScholarshipName() != null) {
						honorsVal = " " + eb.getScholarship().getScholarshipName();
					}
					map.put("III.EB_College_Scholarship_Academic_Honors_Received", honorsVal);
				}

			} else {
				gradopulated = true;
				map.put("III.EB_GraduateStudies_School", " " + eb.getEffectiveSchoolName());

				if(eb.getDegreeCourse() != null) {
					if(eb.getDegreeCourse().getDegreeCourseName() != null) {
						map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course", " " + eb.getDegreeCourse().getDegreeCourseName());
					} else {
						map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course", "");
					}
				} else {
					map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course", "");
				}

				map.put("III.EB_GraduateStudies_Period_Of_Attendance_From",  " " + formatDateMonthYearOnly(eb.getStartDate()));
				map.put("III.EB_GraduateStudies_Period_Of_Attendance_To",  " " + formatDateMonthYearOnly(eb.getEndDate()));
				map.put("III.EB_GraduateStudies_HighestLvl_UnitsEarned", " " + eb.getUnitsEarned());
				map.put("III.EB_GraduateStudies_Year_Graduated", " " + eb.getYearGraduated());

				{
					String honorsVal = "";
					if (eb.getAcademicHonors() != null && eb.getAcademicHonors().getAcademicHonorsName() != null) {
						honorsVal = " " + eb.getAcademicHonors().getAcademicHonorsName();
					} else if (eb.getScholarship() != null && eb.getScholarship().getScholarshipName() != null) {
						honorsVal = " " + eb.getScholarship().getScholarshipName();
					}
					map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", honorsVal);
				}
			}
		}
		
		if(elemPopulated) {
			
		} else {
			map.put("III.EB_Elementary_School", "");
			map.put("III.EB_Elementary_BasicEducation_Degree_Course", "");
			map.put("III.EB_Elementary_Period_Of_Attendance_From", "");
			map.put("III.EB_Elementary_Period_Of_Attendance_To", "");
			map.put("III.EB_Elementary_HighestLvl_UnitsEarned", "");
			map.put("III.EB_Elementary_Year_Graduated", "");
			map.put("III.EB_Elementary_Scholarship_Acad_Honors_Recieved", "");
		}
		
		if(secPopulated) {
			
		} else {
			map.put("III.EB_Secondary_School", "");
			map.put("III.EB_Secondary_BasicEducation_Degree_Course", "");
			map.put("III.EB_Secondary_Period_Of_Attendance_From",  "");
			map.put("III.EB_Secondary_Period_Of_Attendance_To", "");
			map.put("III.EB_Secondary_HighestLvl_UnitsEarned", "");
			map.put("III.EB_Secondary_Year_Graduated", "");
			map.put("III.EB_Secondary_Scholarship_Academic_Honors_Recieved", "");
		}
		
		if(vocPopulated) {
			
		} else {
			map.put("III.EB_Vocational_TradeCourse_School", "");
			map.put("III.EB_Vocational_TradeCourse_Basic_Education_Degree_Course",  "");
			map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_From",  "");
			map.put("III.EB_Vocational_TradeCourse_Period_Of_Attendance_To", "");
			map.put("III.EB_Vocational_TradeCourse_HighestLvl_UnitsEarned", "");
			map.put("III.EB_Vocational_TradeCourse_Year_Graduated", "");
			map.put("III.EB_Vocational_TradeCourse_Scholarship_Academic_Honors_Received", "");
		}
		
		if(collegePopulated) {
			
		} else {
			map.put("III.EB_College_School", "");
			map.put("III.EB_College_BasicEducation_Degree_Course",  "");
			map.put("III.EB_College_Period_Of_Attendance_From",  "");
			map.put("III.EB_College_Period_Of_Attendance_To", "");
			map.put("III.EB_College_HighestLvl_UnitsEarned", "");
			map.put("III.EB_College_Year_Graduated", "");
			map.put("III.EB_College_Scholarship_Academic_Honors_Received", "");
		}
		
		if(gradopulated) {
			
		} else {
			map.put("III.EB_GraduateStudies_School", "");
			map.put("III.EB_GraduateStudies_BasicEducation_Degree_Course",  "");
			map.put("III.EB_GraduateStudies_Period_Of_Attendance_From",  "");
			map.put("III.EB_GraduateStudies_Period_Of_Attendance_To",  "");
			map.put("III.EB_GraduateStudies_HighestLvl_UnitsEarned", "");
			map.put("III.EB_GraduateStudies_Year_Graduated", "");
			map.put("III.EB_GraduateStudies_Scholarship_Academic_Honors_Received", "");
		}
		
		
		
		map.put("III.EB_Signature", "");
		map.put("III.EB_Date", "");
		map.put("CopyOFParameter_30", "");
		map.put("CopyOFParameter_31", "");
		map.put("CopyOFParameter_32", "");
		map.put("CopyOFParameter_33", "");
		map.put("CopyOFParameter_34", "");
		map.put("CopyOFParameter_35", "");
		map.put("CopyOFParameter_36", "");
		map.put("CopyOFParameter_37", "");	
		
		
		
		return map;
	}
	
	private Map<String, Object> populateMapReport2(List<CivilServiceEligibility> csList, List<WorkExperience> workExList) throws FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		File file = ResourceUtils.getFile("classpath:static/images/PDS2.png");
		String bgImg = file.getAbsolutePath();
		
		map.put("FormBg2", bgImg);
		
		int ctrForCS = 1;
		for(CivilServiceEligibility cs : csList) {
			map.put("IV.CSE_Career_Service_RA_1080_"+ctrForCS, " " + getStringValue(cs.getEligibility()) );
			map.put("IV.CSE_Rating"+ctrForCS, " " + getStringValue(cs.getRating()) );
			map.put("IV.CSE_Date_Of_Examination"+ctrForCS, " " + getStringValue(cs.getExamDate()) );
			map.put("IV.CSE_Place_Of_Examination"+ctrForCS, " " + getStringValue(cs.getPlaceOfExam()) );
			map.put("IV.CSE_License_Number_"+ctrForCS, " " + getStringValue(cs.getLicenseNo()) );
			map.put("IV.CSE_License_Date_Of_Validity"+ctrForCS, " " + formatDateMonthYearOnly(cs.getLicenseValidityDate()) );
			ctrForCS++;
		}
		
		if(ctrForCS < 7) {
			for(int x = ctrForCS; x <= 7; x++) {				
				map.put("IV.CSE_Career_Service_RA_1080_"+x, "");
				map.put("IV.CSE_Rating"+x, "");
				map.put("IV.CSE_Date_Of_Examination"+x, "");
				map.put("IV.CSE_Place_Of_Examination"+x, "");
				map.put("IV.CSE_License_Number_"+x, "");
				map.put("IV.CSE_License_Date_Of_Validity"+x, "");
			}
		}
		
		int ctrForWorkEx = 1;
		for(WorkExperience we : workExList) {
			map.put("V.WE_Inclusive_Dates_From"+ctrForWorkEx, " " + formatDateMonthYearOnly(we.getDateFrom()) );
			map.put("V.WE_Inclusive_Dates_To"+ctrForWorkEx, " " + formatDateMonthYearOnly(we.getDateTo()) );
			map.put("V.WE_Position_Title"+ctrForWorkEx, " " + getStringValue(we.getPositionTitle()) );
			map.put("V.WE_Department_Agency_Office_Company"+ctrForWorkEx, " " + getStringValue(we.getDepartment()) );
			map.put("V.WE_Montly_Salary"+ctrForWorkEx, " " + getStringValueFromBigDecimal(we.getSalary()) );
			map.put("V.WE_Salary_Job_PayGrade"+ctrForWorkEx, " " + getStringValue(we.getSalaryGrade() + "") );
			map.put("V.WE_Status_Of_Appointment"+ctrForWorkEx, " " + getStringValue(we.getAppointmentStatus()) );
			map.put("V.WE_Gov_Service"+ctrForWorkEx, " " + getStringValue(we.getGovtOffice()) );
			ctrForWorkEx++;
		}
		
		if(ctrForWorkEx < 28) {
			for(int x = ctrForWorkEx; x <= 28; x++) {				
				map.put("V.WE_Inclusive_Dates_From"+x, "");
				map.put("V.WE_Inclusive_Dates_To"+x, "");
				map.put("V.WE_Position_Title"+x, "");
				map.put("V.WE_Department_Agency_Office_Company"+x, "");
				map.put("V.WE_Montly_Salary"+x, "");
				map.put("V.WE_Salary_Job_PayGrade"+x, "");
				map.put("V.WE_Status_Of_Appointment"+x, "");
				map.put("V.WE_Gov_Service"+x, "");
			}
		}
		
		map.put("V.WE_SIGNATURE", "");
		map.put("V.WE_DATE", "");
		
		
		return map;
	}
	
	private Map<String, Object> populateMapReport3(List<VoluntaryWork> voluntaryList, List<LearningAndDevelopment> learningList, List<OtherInfo> otherList) throws FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		File file = ResourceUtils.getFile("classpath:static/images/PDS3.png");
		String bgImg = file.getAbsolutePath();
		
		map.put("FormBg3", bgImg);
		
		int ctrForVw = 1;
		for(VoluntaryWork vw : voluntaryList) {
			map.put("VI.VW_Name_Address_Of_Org"+ctrForVw, " " + getStringValue(vw.getOrgName()) );
			map.put("VI.VW_Inclusive_Dates_From"+ctrForVw, " " + formatDateMonthYearOnly(vw.getDateFrom()) );
			map.put("VI.VW_Inclusive_Dates_To"+ctrForVw, " " + formatDateMonthYearOnly(vw.getDateTo()) );
			map.put("VI.VW_Number_Of_Hours"+ctrForVw, " " + getStringValue(vw.getNoHours() + "") );
			map.put("VI.VW_Position_Nature_Of_Work"+ctrForVw, " " + getStringValue(vw.getNatureOfWork()) );
			ctrForVw++;
		}
		
		if(ctrForVw < 7) {
			for(int x = ctrForVw; x <= 7; x++) {				
				map.put("VI.VW_Name_Address_Of_Org"+x, "");
				map.put("VI.VW_Inclusive_Dates_From"+x, "");
				map.put("VI.VW_Inclusive_Dates_To"+x, "");
				map.put("VI.VW_Number_Of_Hours"+x, "");
				map.put("VI.VW_Position_Nature_Of_Work"+x, "");
			}
		}
		
		int ctrForLd = 1;
		for(LearningAndDevelopment ld : learningList) {
			map.put("VII.LAD_Training_Programs"+ctrForLd, " " + getStringValue(ld.getTitleOfSeminar()) );
			map.put("VII.LAD_Inclusive_Dates_Of_Attendance_From"+ctrForLd, " " + formatDateMonthYearOnly(ld.getDateFrom()) );
			map.put("VII.LAD_Inclusive_Dates_Of_Attendance_To"+ctrForLd, " " + formatDateMonthYearOnly(ld.getDateTo()) );			
			map.put("VII.LAD_Number_Of_Hours"+ctrForLd,  " " + getStringValue(ld.getNoHours() + "") );
			map.put("VII.LAD_Type_Of_LD"+ctrForLd, " " + getStringValue(ld.getLearningType()) );
			map.put("VII.Conducted_Sponsored_By"+ctrForLd, " " + getStringValue(ld.getProviders()) );
			ctrForLd++;
		}
		
		if(ctrForLd < 21) {
			for(int x = ctrForLd; x <= 21; x++) {				
				map.put("VII.LAD_Training_Programs"+x, "");
				map.put("VII.LAD_Inclusive_Dates_Of_Attendance_From"+x, "");
				map.put("VII.LAD_Inclusive_Dates_Of_Attendance_To"+x, "");	
				map.put("VII.LAD_Number_Of_Hours"+x, "");
				map.put("VII.LAD_Type_Of_LD"+x, "");
				map.put("VII.Conducted_Sponsored_By"+x, "");
			}
		}
		
		int ctrForOtherInfo = 1;
		for(OtherInfo oi : otherList) {
			map.put("VIII.OI_Special_Skills_Hobbies"+ctrForOtherInfo, " " + getStringValue(oi.getSpecialSkill()) );
			map.put("VIII.OI_NonAcademic_Distinctions_Recognitions"+ctrForOtherInfo, " " + getStringValue(oi.getNonAcademic()) );
			map.put("VIII.OI_Membership_In_Association"+ctrForOtherInfo, " " + getStringValue(oi.getMembershipInAssociation()) );
			ctrForOtherInfo++;
		}
		
		if(ctrForOtherInfo < 7) {
			for(int x = ctrForOtherInfo; x <= 7; x++) {				
				map.put("VIII.OI_Special_Skills_Hobbies"+x, "");
				map.put("VIII.OI_NonAcademic_Distinctions_Recognitions"+x, "");
				map.put("VIII.OI_Membership_In_Association"+x, "");
			}
		}
		
		map.put("VIII.OI_Signature", "");
		map.put("VIII.OI_Date", "");
		
		
		return map;
	}
	
	private Map<String, Object> populateMapReport4(OtherInfoQuestion otherInfoQuestion, List<EmpReferences> referencesList, List<GovermentIssuedId> govIdList) throws FileNotFoundException {
		Map<String, Object> map = new HashMap<String, Object>();
		File file = ResourceUtils.getFile("classpath:static/images/PDS4.png");
		String bgImg = file.getAbsolutePath();
		
		map.put("FormBg4", bgImg);
		
		//34 A
		if(otherInfoQuestion.getQuestionOneThird() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionOneThird())) {
				map.put("VIII.OI_34_A_Yes", "X");
				map.put("VIII.OI_34_A_No", "");
			} else {
				map.put("VIII.OI_34_A_Yes", "");
				map.put("VIII.OI_34_A_No", "X");
			}
		} else {
			map.put("VIII.OI_34_A_Yes", "");
			map.put("VIII.OI_34_A_No", "");
		}
		
		//34 B
		if(otherInfoQuestion.getQuestionOneFourth() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionOneFourth())) {
				map.put("VIII.OI_34_B_Yes", "X");
				map.put("VIII.OI_34_B_No", "");
				if(otherInfoQuestion.getQuestionOneFourthIfYes() != null 
						&& otherInfoQuestion.getQuestionOneFourthIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionOneFourthIfYes())) {
					map.put("VIII.OI_34_If_Yes", otherInfoQuestion.getQuestionOneFourthIfYes());
				} else {
					map.put("VIII.OI_34_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_34_B_Yes", "");
				map.put("VIII.OI_34_B_No", "X");
				map.put("VIII.OI_34_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_34_B_Yes", "");
			map.put("VIII.OI_34_B_No", "");
			map.put("VIII.OI_34_If_Yes", "");
		}
		
		//35 A
		if(otherInfoQuestion.getQuestionTwoA() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoA())) {
				map.put("VIII.OI_35_A_Yes", "X");
				map.put("VIII.OI_35_A_No", "");
				if(otherInfoQuestion.getQuestionTwoAIfYes() != null 
						&& otherInfoQuestion.getQuestionTwoAIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoAIfYes())) {
					map.put("VIII.OI_35_A_If_Yes", otherInfoQuestion.getQuestionTwoAIfYes());
				} else {
					map.put("VIII.OI_35_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_35_A_Yes", "");
				map.put("VIII.OI_35_A_No", "X");
				map.put("VIII.OI_35_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_35_A_Yes", "");
			map.put("VIII.OI_35_A_No", "");
			map.put("VIII.OI_35_A_If_Yes", "");
		}
		
		//35 B
		if(otherInfoQuestion.getQuestionTwoB() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoB())) {
				map.put("VIII.OI_35_B_Yes", "X");
				map.put("VIII.OI_35_B_No", "");
				if(otherInfoQuestion.getQuestionTwoBIfYes() != null 
						&& otherInfoQuestion.getQuestionTwoBIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionTwoBIfYes())) {
					map.put("VIII.OI_35_Date_Filed", otherInfoQuestion.getQuestionTwoBMonth() + " " + otherInfoQuestion.getQuestionTwoBDay() + " " + otherInfoQuestion.getQuestionTwoBYear());
					map.put("VIII.OI_35_Status_Of_Cases", otherInfoQuestion.getQuestionTwoBStatusCase());
				} else {
					map.put("VIII.OI_35_Date_Filed", "");
					map.put("VIII.OI_35_Status_Of_Cases", "");
				}
			} else {
				map.put("VIII.OI_35_B_Yes", "");
				map.put("VIII.OI_35_B_No", "X");
				map.put("VIII.OI_35_Date_Filed", "");
				map.put("VIII.OI_35_Status_Of_Cases", "");
			}
		} else {
			map.put("VIII.OI_35_B_Yes", "");
			map.put("VIII.OI_35_B_No", "");
			map.put("VIII.OI_35_Date_Filed", "");
			map.put("VIII.OI_35_Status_Of_Cases", "");			
		}
		
		//36	
		if(otherInfoQuestion.getQuestionThree() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionThree())) {
				map.put("VIII.OI_36_A_Yes", "X");
				map.put("VIII.OI_36_A_No", "");
				if(otherInfoQuestion.getQuestionThreeIfYes() != null 
						&& otherInfoQuestion.getQuestionThreeIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionThreeIfYes())) {
					map.put("VIII.OI_36_If_Yes", otherInfoQuestion.getQuestionThreeIfYes());
				} else {
					map.put("VIII.OI_36_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_36_A_Yes", "");
				map.put("VIII.OI_36_A_No", "X");
				map.put("VIII.OI_36_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_36_A_Yes", "");
			map.put("VIII.OI_36_A_No", "");
			map.put("VIII.OI_36_If_Yes", "");
		}
		
		//37
		if(otherInfoQuestion.getQuestionFour() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionFour())) {
				map.put("VIII.OI_37_A_Yes", "X");
				map.put("VIII.OI_37_A_No", "");
				if(otherInfoQuestion.getQuestionFourIfYes() != null 
						&& otherInfoQuestion.getQuestionFourIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionFourIfYes())) {
					map.put("VIII.OI_37_A_If_Yes", otherInfoQuestion.getQuestionFourIfYes());
				} else {
					map.put("VIII.OI_37_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_37_A_Yes", "");
				map.put("VIII.OI_37_A_No", "X");
				map.put("VIII.OI_37_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_37_A_Yes", "");
			map.put("VIII.OI_37_A_No", "");
			map.put("VIII.OI_37_A_If_Yes", "");
		}
		
		//38 A
		if(otherInfoQuestion.getQuestionFive() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionFive())) {
				map.put("VIII.OI_38_A_Yes", "X");
				map.put("VIII.OI_38_A_No", "");
				if(otherInfoQuestion.getQuestionFiveIfYes() != null 
						&& otherInfoQuestion.getQuestionFiveIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionFiveIfYes())) {
					map.put("VIII.OI_38_A_If_Yes", otherInfoQuestion.getQuestionFiveIfYes());
				} else {
					map.put("VIII.OI_38_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_38_A_Yes", "");
				map.put("VIII.OI_38_A_No", "X");
				map.put("VIII.OI_38_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_38_A_Yes", "");
			map.put("VIII.OI_38_A_No", "");
			map.put("VIII.OI_38_A_If_Yes", "");
		}
		
		//38 B
		if(otherInfoQuestion.getQuestionSix() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionSix())) {
				map.put("VIII.OI_38_B_Yes", "X");
				map.put("VIII.OI_38_B_No", "");
				if(otherInfoQuestion.getQuestionSixIfYes() != null 
						&& otherInfoQuestion.getQuestionSixIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionSixIfYes())) {
					map.put("VIII.OI_38_B_If_Yes", otherInfoQuestion.getQuestionSixIfYes());
				} else {
					map.put("VIII.OI_38_B_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_38_B_Yes", "");
				map.put("VIII.OI_38_B_No", "X");
				map.put("VIII.OI_38_B_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_38_B_Yes", "");
			map.put("VIII.OI_38_B_No", "");
			map.put("VIII.OI_38_B_If_Yes", "");
		}
		
		//39
		if(otherInfoQuestion.getQuestionSevenA() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionSevenA())) {
				map.put("VIII.OI_39_A_Yes", "X");
				map.put("VIII.OI_39_A_No", "");
				if(otherInfoQuestion.getQuestionSevenAIfYes() != null 
						&& otherInfoQuestion.getQuestionSevenAIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionSevenAIfYes())) {
					map.put("VIII.OI_39_A_If_Yes", otherInfoQuestion.getQuestionSevenAIfYes());
				} else {
					map.put("VIII.OI_39_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_39_A_Yes", "");
				map.put("VIII.OI_39_A_No", "X");
				map.put("VIII.OI_39_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_39_A_Yes", "");
			map.put("VIII.OI_39_A_No", "");
			map.put("VIII.OI_39_A_If_Yes", "");
		}
		
		//40 C
		if(otherInfoQuestion.getQuestionNine() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionNine())) {
				map.put("VIII.OI_40_C_Yes", "X");
				map.put("VIII.OI_40_C_No", "");
				if(otherInfoQuestion.getQuestionNineIfYes() != null 
						&& otherInfoQuestion.getQuestionNineIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionNineIfYes())) {
					map.put("VIII.OI_40_C_If_Yes", otherInfoQuestion.getQuestionNineIfYes());
				} else {
					map.put("VIII.OI_40_C_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_40_C_Yes", "");
				map.put("VIII.OI_40_C_No", "X");
				map.put("VIII.OI_40_C_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_40_C_Yes", "");
			map.put("VIII.OI_40_C_No", "");
			map.put("VIII.OI_40_C_If_Yes", "");
		}
		
		//40 A
		if(otherInfoQuestion.getQuestionTen() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionTen())) {
				map.put("VIII.OI_40_A_Yes", "X");
				map.put("VIII.OI_40_A_No", "");
				if(otherInfoQuestion.getQuestionTenIfYes() != null 
						&& otherInfoQuestion.getQuestionTenIfYes().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionTenIfYes())) {
					map.put("VIII.OI_40_A_If_Yes", otherInfoQuestion.getQuestionTenIfYes());
				} else {
					map.put("VIII.OI_40_A_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_40_A_Yes", "");
				map.put("VIII.OI_40_A_No", "X");
				map.put("VIII.OI_40_A_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_40_A_Yes", "");
			map.put("VIII.OI_40_A_No", "");
			map.put("VIII.OI_40_A_If_Yes", "");
		}
		
		//40 B
		if(otherInfoQuestion.getQuestionEight() != null) {
			if("YES".equalsIgnoreCase(otherInfoQuestion.getQuestionEight())) {
				map.put("VIII.OI_40_B_Yes", "X");
				map.put("VIII.OI_40_B_No", "");
				if(otherInfoQuestion.getQuestionEightId() != null 
						&& otherInfoQuestion.getQuestionEightId().length() > 0 
						&& !"null".equalsIgnoreCase(otherInfoQuestion.getQuestionEightId())) {
					map.put("VIII.OI_40_B_If_Yes", otherInfoQuestion.getQuestionEightId());
				} else {
					map.put("VIII.OI_40_B_If_Yes", "");
				}
			} else {
				map.put("VIII.OI_40_B_Yes", "");
				map.put("VIII.OI_40_B_No", "X");
				map.put("VIII.OI_40_B_If_Yes", "");
			}
		} else {
			map.put("VIII.OI_40_B_Yes", "");
			map.put("VIII.OI_40_B_No", "");
			map.put("VIII.OI_40_B_If_Yes", "");
		}
		
		
		
		//References
		int ctrForRef = 1;
		for(EmpReferences ref : referencesList) {
			map.put("VIII.OI_41_References_Name"+ctrForRef, " " + getStringValue(ref.getReferenceName()) );
			map.put("VIII.OI_41_References_Address"+ctrForRef, " " + getStringValue(ref.getCompanyAddress()) );
			map.put("VIII.OI_41_References_Tel_No"+ctrForRef, " " + getStringValue(ref.getCompanyContactNo()) );
			ctrForRef++;
		}
		
		if(ctrForRef < 3) {
			for(int x = ctrForRef; x <= 7; x++) {				
				map.put("VIII.OI_41_References_Name"+x, "");
				map.put("VIII.OI_41_References_Address"+x, "");
				map.put("VIII.OI_41_References_Tel_No"+x, "");
			}
		}
		
		if (!govIdList.isEmpty()) {
            // Return the first item in the list
			GovermentIssuedId obj =  govIdList.get(0);
			map.put("VIII.OI_42_Gov_ID", obj.getGovermentIssuedName());
			map.put("VIII.OI_42_ID_License_Passport_No", obj.getIdNo());
			map.put("VIII.OI_42_Date_Place_Of_Issurance", obj.getPlaceOfIssuance());
			
		} else {
			map.put("VIII.OI_42_Gov_ID", "");
			map.put("VIII.OI_42_ID_License_Passport_No", "");
			map.put("VIII.OI_42_Date_Place_Of_Issurance", "");
		}
		
		map.put("VIII.OI_42_Signature", "");
		map.put("VIII.OI_42_Date_Accomplished", "");
		map.put("VIII.OI_42_Thumbmark", "");
		map.put("Subscribed_Sworn", "");
		map.put("Person_Administering Oath", "");
		
		//map.put("", "");
		
		return map;
	}
	
	private Map<String, Object> populateMapReport2025_P1(Employee emp, List<FamilyBg> fbList, List<EducationalBackground> eduList) throws FileNotFoundException {
		// Reuse the existing population logic, then overlay 2025-specific fields and date formats.
		Map<String, Object> map = populateMapReport1(emp, fbList, eduList);

		File p1Bg = ResourceUtils.getFile("classpath:static/images/PDS2025_P1.png");
		map.put("FormBg", p1Bg.getAbsolutePath());

		// Item 5 — Sex at Birth (2025 wording). Emit alongside legacy I.PI_Sex_M/F keys.
		if("M".equalsIgnoreCase(emp.getGender())) {
			map.put("I.PI_Sex_At_Birth_M", " X");
			map.put("I.PI_Sex_At_Birth_F", "");
		} else {
			map.put("I.PI_Sex_At_Birth_M", "");
			map.put("I.PI_Sex_At_Birth_F", " X");
		}

		// Items 10 and 13 — UMID and PhilSys (new in 2025).
		map.put("I.PI_UMID_ID_NO", "  " + getDisplayValue(emp.getUmidNo()));
		map.put("I.PI_PhilSys_PSN", "  " + getDisplayValue(emp.getPhilsysNo()));

		// Maiden name — applicable to married female employees.
		map.put("I.PI_MaidenName", "  " + getStringValue(emp.getMaidenName()));

		// Item 3 — date of birth re-formatted dd/MM/yyyy (2025 form spec).
		map.put("I.PI_Date_Of_Birth", "  " + formatDateDdMmYyyy(emp.getBirthdate()));

		// Item 23 — children's birthdays re-formatted dd/MM/yyyy.
		int ctrChild = 1;
		for(FamilyBg fb : fbList) {
			if(!"SPOUSE".equalsIgnoreCase(fb.getRelationship())
					&& !"FATHER".equalsIgnoreCase(fb.getRelationship())
					&& !"MOTHER".equalsIgnoreCase(fb.getRelationship())) {
				map.put("II.FBG_Child_Birthday"+ctrChild, "  " + formatDateOrNA(fb.getBirthdate()));
				ctrChild++;
			}
		}

		// Item 26 — educational-background period dates re-formatted dd/MM/yyyy.
		for(EducationalBackground eb : eduList) {
			String lvl = (eb.getDegreeLevel() != null) ? eb.getDegreeLevel().getDegreeName() : "";
			String prefix;
			if("ELEMENTARY".equalsIgnoreCase(lvl)) prefix = "III.EB_Elementary";
			else if("SECONDARY".equalsIgnoreCase(lvl)) prefix = "III.EB_Secondary";
			else if("VOCATIONAL".equalsIgnoreCase(lvl)) prefix = "III.EB_Vocational_TradeCourse";
			else if("COLLEGE".equalsIgnoreCase(lvl)) prefix = "III.EB_College";
			else prefix = "III.EB_GraduateStudies";
			map.put(prefix + "_Period_Of_Attendance_From", " " + formatDateOrNA(eb.getStartDate()));
			map.put(prefix + "_Period_Of_Attendance_To", eb.isUpToPresent() ? " PRESENT" : " " + formatDateOrNA(eb.getEndDate()));
		}

		return map;
	}

	private Map<String, Object> populateMapReport2025_P2(List<CivilServiceEligibility> csList, List<WorkExperience> workExList) throws FileNotFoundException {
		Map<String, Object> map = populateMapReport2(csList, workExList);

		File p2Bg = ResourceUtils.getFile("classpath:static/images/PDS2025_P2.png");
		map.put("FormBg2", p2Bg.getAbsolutePath());

		// Item 27 — eligibility exam dates and license validity re-formatted dd/MM/yyyy.
		// CivilServiceEligibility stores exam date as separate examDay/examMonth/examYear fields.
		int i = 1;
		for(CivilServiceEligibility cs : csList) {
			map.put("IV.CSE_Date_Of_Examination"+i, " " + formatExamDateDdMmYyyy(cs));
			map.put("IV.CSE_License_Date_Of_Validity"+i, " " + formatDateOrNA(cs.getLicenseValidityDate()));
			i++;
		}

		// Item 28 — work-experience inclusive dates re-formatted dd/MM/yyyy.
		int j = 1;
		for(WorkExperience we : workExList) {
			map.put("V.WE_Inclusive_Dates_From"+j, " " + formatDateOrNA(we.getDateFrom()));
			map.put("V.WE_Inclusive_Dates_To"+j, we.isUpToPresent() ? " PRESENT" : " " + formatDateOrNA(we.getDateTo()));
			j++;
		}

		return map;
	}

	private Map<String, Object> populateMapReport2025_P3(List<VoluntaryWork> voluntaryList, List<LearningAndDevelopment> learningList, List<OtherInfo> otherList) throws FileNotFoundException {
		Map<String, Object> map = populateMapReport3(voluntaryList, learningList, otherList);

		File p3Bg = ResourceUtils.getFile("classpath:static/images/PDS2025_P3.png");
		map.put("FormBg3", p3Bg.getAbsolutePath());

		// Item 29 — voluntary-work inclusive dates re-formatted dd/MM/yyyy.
		int i = 1;
		for(VoluntaryWork vw : voluntaryList) {
			map.put("VI.VW_Inclusive_Dates_From"+i, " " + formatDateDdMmYyyy(vw.getDateFrom()));
			map.put("VI.VW_Inclusive_Dates_To"+i, " " + formatDateDdMmYyyy(vw.getDateTo()));
			i++;
		}

		// Item 30 — L&D inclusive dates re-formatted dd/MM/yyyy.
		int j = 1;
		for(LearningAndDevelopment ld : learningList) {
			map.put("VII.LAD_Inclusive_Dates_Of_Attendance_From"+j, " " + formatDateDdMmYyyy(ld.getDateFrom()));
			map.put("VII.LAD_Inclusive_Dates_Of_Attendance_To"+j, " " + formatDateDdMmYyyy(ld.getDateTo()));
			j++;
		}

		return map;
	}

	private Map<String, Object> populateMapReport2025_P4(OtherInfoQuestion otherInfoQuestion, List<EmpReferences> referencesList, List<GovermentIssuedId> govIdList) throws FileNotFoundException {
		Map<String, Object> map = populateMapReport4(otherInfoQuestion, referencesList, govIdList);

		File p4Bg = ResourceUtils.getFile("classpath:static/images/PDS2025_P4.png");
		map.put("FormBg4", p4Bg.getAbsolutePath());

		return map;
	}

	// CivilServiceEligibility stores the exam date as three separate fields
	// (examDay int, examMonth String like "JAN", examYear int). Assemble them
	// into dd/MM/yyyy for the 2025 PDS form. If day or year is missing/zero we
	// fall back to the legacy "MMM YYYY" rendering used by populateMapReport2.
	private static String formatExamDateDdMmYyyy(CivilServiceEligibility cs) {
		if(cs == null) return "";
		int day = cs.getExamDay();
		String month = cs.getExamMonth();
		int year = cs.getExamYear();
		if(day > 0 && month != null && !month.isBlank() && year > 0) {
			int mm = monthAbbrevToNumber(month);
			if(mm > 0) {
				return String.format("%02d/%02d/%04d", day, mm, year);
			}
		}
		return getStringValue(cs.getExamDate());
	}

	private static int monthAbbrevToNumber(String m) {
		if(m == null) return 0;
		switch(m.trim().toUpperCase()) {
			case "JAN": return 1;
			case "FEB": return 2;
			case "MAR": return 3;
			case "APR": return 4;
			case "MAY": return 5;
			case "JUN": return 6;
			case "JUL": return 7;
			case "AUG": return 8;
			case "SEPT": case "SEP": return 9;
			case "OCT": return 10;
			case "NOV": return 11;
			case "DEC": return 12;
			default: return 0;
		}
	}

	@GetMapping("/viewEmployeeListReport")
	public void viewEmployeeListReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
	}
	
	@GetMapping("/viewServiceRecordReportNew/{recordId}")
	public void viewServiceRecordReportNew(Model model, @PathVariable long recordId, 
//			@PathVariable String signatory,
//			@PathVariable String position,
//			@PathVariable String notes,
			HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		Optional<ServiceRecordReportRequest> optionalSrRequest = serviceRecordReportRequestRepository.findById(recordId);
		if (!optionalSrRequest.isPresent()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Service record request not found.");
			return;
		}
		ServiceRecordReportRequest srReportRequest = optionalSrRequest.get();
		if (srReportRequest.getEmployee() == null) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Employee not linked to service record request.");
			return;
		}

		Optional<Employee> optional = employeeRepository.findById(srReportRequest.getEmployee().getId());
		Employee employee = optional.orElseGet(() -> new Employee());

		List<ServiceRecord> list = serviceRecordRepository.findByEmployeeId(srReportRequest.getEmployee().getId());
		
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		
		for(ServiceRecord sr : list) {
			reportList.add(convertToDto(sr));
		}
		
		Optional<ServiceRecordSignatory> srsOptional = serviceRecordSignatoryRepository.findAll().stream().findFirst();
		
		ServiceRecordSignatory srsObj = new ServiceRecordSignatory();
		if(srsOptional.isPresent()) {
			srsObj = srsOptional.get();		
		}
		
		
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Surname", employee.getLastName());
		map.put("Given_Name", employee.getFirstName());	
		map.put("Middle_Name", employee.getMiddleName());	
		map.put("BirthDate", formatDate(employee.getBirthdate()));	
		map.put("BirthPlace", employee.getBirthPlace());	
		map.put("Purpose", srReportRequest.getNotes() != null ? srReportRequest.getNotes() : "");	
		map.put("Officer", srsObj.getSignatory() != null ? srsObj.getSignatory() : "");	
		map.put("OfficerPosition", srsObj.getPosition() != null ? srsObj.getPosition() : "");	
		
//		map.put("Officer", "");	
//		map.put("OfficerPosition", "");	
		LocalDate srPrintDate = srReportRequest.getPrintDate();
		map.put("signDate", srPrintDate != null ? formatDate(srPrintDate.plusDays(1)) : "");
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jasper/reports/Service-Record-Form.jasper");

		if(reportStream == null){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report template not found.");
			return;
		}

		JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map, beanColDataSource);

	}

	@GetMapping("/viewServiceRecordReport/{employeeId}")
	public void viewServiceRecordReport(Model model, @PathVariable long employeeId, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		List<ServiceRecord> list = serviceRecordRepository.findByEmployeeId(employeeId);
		Optional<Employee> optional = employeeRepository.findById(employeeId);
		Employee employee = optional.orElseGet(() -> new Employee());
		
		List<ServiceRecordReportDto> reportList = new ArrayList<>(); 
		
		for(ServiceRecord sr : list) {
			reportList.add(convertToDto(sr));
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("Surname", employee.getLastName());
		map.put("Given_Name", employee.getFirstName());	
		map.put("Middle_Name", employee.getMiddleName());	
		map.put("BirthDate", formatDate(employee.getBirthdate()));	
		map.put("BirthPlace", employee.getBirthPlace());	
		map.put("Purpose", "For Cooperative Membership");	
		map.put("Officer", "RIZALINO A. ABUSMAN");	
		map.put("OfficerPosition", "Administrative Officer V");	
		map.put("signDate", formatDate(LocalDate.now()));	
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(reportList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("jasper/reports/Service-Record-Form.jasper");

		if(reportStream == null){
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Report template not found.");
			return;
		}

		JasperRunManager.runReportToPdfStream(reportStream, response.getOutputStream(), map, beanColDataSource);
		
	}
	
	@GetMapping("/viewClearanceForm/{id}")
	public void viewClearanceReport(Model model, @PathVariable long id, HttpServletRequest request, HttpServletResponse response) throws JRException, Exception {
		
		Optional<ClearanceApprovers> optional = clearanceApproversRepository.findAll().stream().findFirst();
		
		ClearanceApprovers clearanceApprovers = new ClearanceApprovers();
		if(optional.isPresent()) {
			clearanceApprovers = optional.get();		
		}
		
		Optional<Clearance> oClearance = clearanceRepository.findById(id);
		if (oClearance.isEmpty()) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Clearance not found");
			return;
		}
		Clearance clearance = oClearance.get();

		Map<String, Object> map = new HashMap<String, Object>();

		String mn = clearance.getEmployee().getMiddleName();
		String middleInitial = (mn != null && !mn.isEmpty()) ? mn.charAt(0) + ". " : "";
		String empName = clearance.getEmployee().getFirstName() + " " + middleInitial + clearance.getEmployee().getLastName();

		String divisionName = clearance.getEmployee().getDivision() != null
				? clearance.getEmployee().getDivision().getDivisionName() : "";
		String positionName = clearance.getEmployee().getPositionTitle() != null
				? clearance.getEmployee().getPositionTitle().getPositionTitleName() : "";

		map.put("City_Gov", HEADER_REPORT_NAME);
		map.put("To_City_Gov", HEADER_REPORT_NAME);
		map.put("Date1", formatDate(clearance.getTransDate()));
		map.put("Date_Effect", formatDate(clearance.getEffectiveDate()));

		map.put("Office_Of_Assignment", divisionName);
		map.put("Position_SG_Step", positionName);
		map.put("Name_Signature_Employee", empName.toUpperCase());
						
		String immediateSupervisor = clearanceApprovers.getImmediateSupervisor();
				
		map.put("Immediate_Supervisor", immediateSupervisor);
				
		String headOfOffice = clearanceApprovers.getHeadOfOffice();
				
		map.put("Head_Of_Office", headOfOffice);
		
		if("TRANSFER".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "X");
			map.put("Retirement", "");
			map.put("Resignation", "");
			map.put("Leave", "");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else if("RETIREMENT".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "");
			map.put("Retirement", "X");
			map.put("Resignation", "");
			map.put("Leave", "");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else if("RESIGNATION".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "");
			map.put("Retirement", "");
			map.put("Resignation", "X");
			map.put("Leave", "");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else if("LEAVE".equalsIgnoreCase(clearance.getPurpose())) {
			map.put("Transfer", "");
			map.put("Retirement", "");
			map.put("Resignation", "");
			map.put("Leave", "X");
			map.put("Other_methods", "");
			map.put("Specify", "");
		} else {
			map.put("Transfer", "");
			map.put("Retirement", "");
			map.put("Resignation", "");
			map.put("Leave", "");
			map.put("Other_methods", "X");
			map.put("Specify", clearance.getOtherPurpose());
		}
		
		
		map.put("Cleared", "");
		map.put("Not_Cleared", "");
		map.put("IV_CONPAC_With_Pending", "");
		map.put("IV_CONPAC_With_Ongoing", "");
		
		String clearingOfficer1A = clearanceApprovers.getAdminPersonA();		
		String clearingOfficer1B = clearanceApprovers.getAdminPersonB();		
		String clearingOfficer1C = clearanceApprovers.getAdminPersonC();		
		
		String clearingOfficer2A = clearanceApprovers.getLibraryPersonA();		
		String clearingOfficer2B = clearanceApprovers.getLibraryPersonB();
		
		String clearingOfficer3A = clearanceApprovers.getFinancePersonA();		
		String clearingOfficer3B = clearanceApprovers.getFinancePersonB();				
		String clearingOfficer3C = clearanceApprovers.getFinancePersonC();		
		
		String clearingOfficer4A = clearanceApprovers.getProfessionalPersonA();	
		
		String clearingOfficer5A = clearanceApprovers.getSection4Person();
		
		//1
		if(clearingOfficer1A != null && clearingOfficer1A.length() > 0) {
			map.put("1A_Name_Clearing_Officer", clearingOfficer1A + "\n" + clearanceApprovers.getAdminPositionA());
//			map.put("1A_Name_Clearing_Officer", "RIZALINO A. ABUSMAN\nAdministrative Officer V");
			map.put("1A_Cleared", "");
			map.put("1A_Not_Cleared", "");
			map.put("1A_Signature", "");
		} else {
			map.put("1A_Name_Clearing_Officer", "N/A");
			map.put("1A_Cleared", "N/A");
			map.put("1A_Not_Cleared", "N/A");
			map.put("1A_Signature", "N/A");
		}
		
		if(clearingOfficer1B != null && clearingOfficer1B.length() > 0) {
			map.put("1B_Name_Clearing_Officer", clearingOfficer1B + "\n" + clearanceApprovers.getAdminPositionB());
//			map.put("1B_Name_Clearing_Officer", "ROSALINDA C. MANOJO\nAdministrative Officer V");
			map.put("1B_Cleared", "");
			map.put("1B_Not_Cleared", "");
			map.put("1B_Signature", "");
		} else {
			map.put("1B_Name_Clearing_Officer", "N/A");
			map.put("1B_Cleared", "N/A");
			map.put("1B_Not_Cleared", "N/A");
			map.put("1B_Signature", "N/A");
		}
		
		if(clearingOfficer1C != null && clearingOfficer1C.length() > 0) {
			map.put("1C_Name_Clearing_Officer", clearingOfficer1C + "\n" + clearanceApprovers.getAdminPositionC());
//			map.put("1C_Name_Clearing_Officer", "");
			map.put("1C_Cleared", "");
			map.put("1C_Not_Cleared", "");
			map.put("1C_Signature", "");
		} else {
			map.put("1C_Name_Clearing_Officer", "N/A");
			map.put("1C_Cleared", "N/A");
			map.put("1C_Not_Cleared", "N/A");
			map.put("1C_Signature", "N/A");
		}
		
		//2
		if(clearingOfficer2A != null && clearingOfficer2A.length() > 0) {
			map.put("2A_Name_Clearing_Officer", clearingOfficer2A + "\n" + clearanceApprovers.getLibraryPositionA());
//			map.put("2A_Name_Clearing_Officer", "HECTOR R. PASCUAL\nAdministrative Officer V");
			map.put("2A_Cleared", "");
			map.put("2A_Not_Cleared", "");
			map.put("2A_Signature", "");
		} else {
			map.put("2A_Name_Clearing_Officer", "N/A");
			map.put("2A_Cleared", "N/A");
			map.put("2A_Not_Cleared", "N/A");
			map.put("2A_Signature", "N/A");
		}
		
		if(clearingOfficer2B != null && clearingOfficer2B.length() > 0) {
			map.put("2B_Name_Clearing_Officer", clearingOfficer2B + "\n" + clearanceApprovers.getLibraryPositionB());
			map.put("2B_Cleared", "");
			map.put("2B_Not_Cleared", "");
			map.put("2B_Signature", "");
		} else {
			map.put("2B_Name_Clearing_Officer", "N/A");
			map.put("2B_Cleared", "N/A");
			map.put("2B_Not_Cleared", "N/A");
			map.put("2B_Signature", "N/A");
		}
		
		//3
		if(clearingOfficer3A != null && clearingOfficer3A.length() > 0) {
			map.put("3A_Name_Clearing_Officer", clearingOfficer3A + "\n" + clearanceApprovers.getFinancePositionA());
			map.put("3A_Cleared", "");
			map.put("3A_Not_Cleared", "");
			map.put("3A_Signature", "");
		} else {
			map.put("3A_Name_Clearing_Officer", "N/A");
			map.put("3A_Cleared", "N/A");
			map.put("3A_Not_Cleared", "N/A");
			map.put("3A_Signature", "N/A");
		}
		
		if(clearingOfficer3B != null && clearingOfficer3B.length() > 0) {
			map.put("3B_Name_Clearing_Officer", clearingOfficer3B + "\n" + clearanceApprovers.getFinancePositionB());
			map.put("3B_Cleared", "");
			map.put("3B_Not_Cleared", "");
			map.put("3B_Signature", "");
		} else {
			map.put("3B_Name_Clearing_Officer", "N/A");
			map.put("3B_Cleared", "N/A");
			map.put("3B_Not_Cleared", "N/A");
			map.put("3B_Signature", "N/A");
		}
		
		if(clearingOfficer3C != null && clearingOfficer3C.length() > 0) {
			map.put("3C_Name_Clearing_Officer", clearingOfficer3C + "\n" + clearanceApprovers.getFinancePositionC());
			map.put("3C_Cleared", "");
			map.put("3C_Not_Cleared", "");
			map.put("3C_Signature", "");
		} else {
			map.put("3C_Name_Clearing_Officer", "N/A");
			map.put("3C_Cleared", "N/A");
			map.put("3C_Not_Cleared", "N/A");
			map.put("3C_Signature", "N/A");
		}
		
		//4
		if(clearingOfficer4A != null && clearingOfficer4A.length() > 0) {
			map.put("4A_Name_Clearing_Officer", clearingOfficer4A + "\n" + clearanceApprovers.getProfessionalPositionA());
			map.put("4A_Cleared", "");
			map.put("4A_Not_Cleared", "");
			map.put("4A_Signature", "");
		} else {
			map.put("4A_Name_Clearing_Officer", "N/A");
			map.put("4A_Cleared", "N/A");
			map.put("4A_Not_Cleared", "N/A");
			map.put("4A_Signature", "N/A");
		}
		
		//5
		if(clearingOfficer5A != null && clearingOfficer5A.length() > 0) {
			map.put("IV_CONPAC_A_Name_Clearing_Officer", clearingOfficer4A + "\n" + clearanceApprovers.getSection4Position());
//			map.put("IV_CONPAC_A_Name_Clearing_Officer", "CHARITO A. RUMBO\nChief Administrative Officer");
			map.put("IV_CONPAC_A_Cleared", "");
			map.put("IV_CONPAC_A_Not_Cleared", "");
			map.put("IV_CONPAC_A_Signature", "");
		} else {
			map.put("IV_CONPAC_A_Name_Clearing_Officer", "N/A");
			map.put("IV_CONPAC_A_Cleared", "N/A");
			map.put("IV_CONPAC_A_Not_Cleared", "N/A");
			map.put("IV_CONPAC_A_Signature", "N/A");
		}
		
		String footerSignatory2 = clearanceApprovers.getFooterPerson1();		
		
		map.put("V_CERTIFICATION_NAME2", footerSignatory2);
//		map.put("V_CERTIFICATION_NAME2", "LUCH R. GEMPIS JR.");
		map.put("V_CERTIFICATION_POSITION2", clearanceApprovers.getFooterPosition2());
		map.put("V_CERTIFICATION_OFFICE2", "Secretary to the City Council");
		
		String footerSignatory1 = clearanceApprovers.getFooterPerson1();
		
		map.put("V_CERTIFICATION_NAME1", footerSignatory1);
//		map.put("V_CERTIFICATION_NAME1", "ROMEO N. FRANCIA");
		map.put("V_CERTIFICATION_POSITION1", clearanceApprovers.getFooterPosition1());
		map.put("V_CERTIFICATION_OFFICE1", "Office of the Assistant Secretary");
				
		
		List<Employee> dataList = new ArrayList<Employee>();	
		
		Employee dummyData = new Employee();
		dummyData.setFirstName("test");
		
		dataList.add(dummyData);
		
		
		JRBeanCollectionDataSource beanColDataSource = new JRBeanCollectionDataSource(dataList);
		
		response.setContentType("application/pdf");
		
		InputStream reportStream = Thread.currentThread().getContextClassLoader().getResourceAsStream( "jasper/reports/Clearance-Form-CSC-Form.jasper");
		
		
		if(reportStream == null){
			System.out.println("reportStream is NULL");
		}
		
		if(response.getOutputStream() == null){
			System.out.println("response.getOutputStream() is NULL");
		}
		
		JasperRunManager.runReportToPdfStream(reportStream,	response.getOutputStream(), map, beanColDataSource);
		
	}
	
	private static String formatDate(LocalDate localDate) {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");
		
		if(localDate != null) {			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
			return localDate.format(formatter);
		}
		
		return "";
	}
	
	private static String formatDateMonthYearOnly(LocalDate localDate) {
//		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd yyyy");		
		
		if(localDate != null) {			
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM");
			return localDate.format(formatter);
		}
		
		return "";
	}
	
	// CS Form No. 212 (Revised 2025) uses dd/MM/yyyy throughout the PDS.
	private static String formatDateDdMmYyyy(LocalDate localDate) {
		if(localDate != null) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
			return localDate.format(formatter);
		}
		return "";
	}

	/** dd/MM/yyyy for optional fields — prints "N/A" when the date is null. */
	private static String formatDateOrNA(LocalDate localDate) {
		if(localDate != null) {
			return localDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
		}
		return "N/A";
	}
	
	private static ServiceRecordReportDto convertToDto(ServiceRecord serviceRecord) {
        ServiceRecordReportDto dto = new ServiceRecordReportDto();
        dto.setDateFrom(formatDate(serviceRecord.getDateFrom()));
        dto.setDateTo(formatDate(serviceRecord.getDateTo()));
        dto.setDesignation(serviceRecord.getDesignation());
        dto.setEmployeeStatus(serviceRecord.getEmployeeStatus() != null ? serviceRecord.getEmployeeStatus().getEmployeeStatusName() : "");
        dto.setSalary(serviceRecord.getSalary() != null ? getFormattedAmount(serviceRecord.getSalary()) : "");
        dto.setStation(serviceRecord.getStation());
        dto.setBranch(serviceRecord.getBranch());
        dto.setLvAbs(serviceRecord.getLvAbs());
        dto.setSeparationCause(serviceRecord.getSeparationCause());
        dto.setSeparationDate(formatDate(serviceRecord.getSeparationDate()));
        return dto;
    }
	
	private static String getFormattedAmount(Object obj){
		DecimalFormat decimalFormat = new DecimalFormat("#,###,###.00");
		return decimalFormat.format(obj);
	}
	
	public static Map<String, String> generateProvinceMap() {
        Map<String, String> provinceMap = new HashMap<>();

        // Add province data to the map
        provinceMap.put("MM", "Metro Manila");
        provinceMap.put("ABR", "Abra");
        provinceMap.put("APA", "Apayao");
        provinceMap.put("BEN", "Benguet");
        provinceMap.put("IFU", "Ifugao");
        provinceMap.put("KAL", "Kalinga");
        provinceMap.put("MOU", "Mountain Province");
        provinceMap.put("ILN", "Ilocos Norte");
        provinceMap.put("ILS", "Ilocos Sur");
        provinceMap.put("LUN", "La Union");
        provinceMap.put("PAN", "Pangasinan");
        provinceMap.put("BTN", "Batanes");
        provinceMap.put("CAG", "Cagayan");
        provinceMap.put("ISA", "Isabela");
        provinceMap.put("NUV", "Nueva Vizcaya");
        provinceMap.put("QUI", "Quirino");
        provinceMap.put("AUR", "Aurora");
        provinceMap.put("BAN", "Bataan");
        provinceMap.put("BUL", "Bulacan");
        provinceMap.put("NUE", "Nueva Ecija");
        provinceMap.put("PAM", "Pampanga");
        provinceMap.put("TAR", "Tarlac");
        provinceMap.put("ZMB", "Zambales");
        provinceMap.put("BTG", "Batangas");
        provinceMap.put("CAV", "Cavite");
        provinceMap.put("LAG", "Laguna");
        provinceMap.put("QUE", "Quezon");
        provinceMap.put("RIZ", "Rizal");
        provinceMap.put("MAD", "Marinduque");
        provinceMap.put("MDC", "Occidental Mindoro");
        provinceMap.put("MDR", "Oriental Mindoro");
        provinceMap.put("PLW", "Palawan");
        provinceMap.put("ROM", "Romblon");
        provinceMap.put("ALB", "Albay");
        provinceMap.put("CAN", "Camarines Norte");
        provinceMap.put("CAS", "Camarines Sur");
        provinceMap.put("CAT", "Catanduanes");
        provinceMap.put("MAS", "Masbate");
        provinceMap.put("SOR", "Sorsogon");
        provinceMap.put("AKL", "Aklan");
        provinceMap.put("ANT", "Antique");
        provinceMap.put("CAP", "Capiz");
        provinceMap.put("GUI", "Guimaras");
        provinceMap.put("ILI", "Iloilo");
        provinceMap.put("NEC", "Negros Occidental");
        provinceMap.put("BOH", "Bohol");
        provinceMap.put("CEB", "Cebu");
        provinceMap.put("NER", "Negros Oriental");
        provinceMap.put("SIG", "Siquijor");
        provinceMap.put("BIL", "Biliran");
        provinceMap.put("EAS", "Eastern Samar");
        provinceMap.put("LEY", "Leyte");
        provinceMap.put("NSA", "Northern Samar");
        provinceMap.put("WSA", "Samar");
        provinceMap.put("SLE", "Southern Leyte");
        provinceMap.put("ZAN", "Zamboanga del Norte");
        provinceMap.put("ZAS", "Zamboanga del Sur");
        provinceMap.put("ZSI", "Zamboanga Sibugay");
        provinceMap.put("BUK", "Bukidnon");
        provinceMap.put("CAM", "Camiguin");
        provinceMap.put("LAN", "Lanao del Norte");
        provinceMap.put("MSC", "Misamis Occidental");
        provinceMap.put("MSR", "Misamis Oriental");
        provinceMap.put("COM", "Compostela Valley");
        provinceMap.put("DAV", "Davao del Norte");
        provinceMap.put("DAS", "Davao del Sur");
        provinceMap.put("DAC", "Davao Occidental");
        provinceMap.put("DAO", "Davao Oriental");
        provinceMap.put("NCO", "Cotabato");
        provinceMap.put("SAR", "Sarangani");
        provinceMap.put("SCO", "South Cotabato");
        provinceMap.put("SUK", "Sultan Kudarat");
        provinceMap.put("AGN", "Agusan del Norte");
        provinceMap.put("AGS", "Agusan del Sur");
        provinceMap.put("DIN", "Dinagat Islands");
        provinceMap.put("SUN", "Surigao del Norte");
        provinceMap.put("SUR", "Surigao del Sur");
        provinceMap.put("BAS", "Basilan");
        provinceMap.put("LAS", "Lanao del Sur");
        provinceMap.put("MAG", "Maguindanao");
        provinceMap.put("SLU", "Sulu");
        provinceMap.put("TAW", "Tawi-tawi");

        return provinceMap;
    }
	
	private static String getStringValueFromBigDecimal(BigDecimal val) {
		if(val != null) {
			DecimalFormat decimalFormat = new DecimalFormat("#,###,###.##");
			return decimalFormat.format(val);
		} else {
			return "";
		}		
	}
	
	private static String getStringValue(String val) {
		if(val != null) {
			if("null".equalsIgnoreCase(val.trim())) {
				return "";
			} else {
				return val;
			}
		} else {
			return "";
		}
	}

	/** Returns the value for display, substituting "N/A" when the value is blank. */
	private static String getDisplayValue(String val) {
		String s = getStringValue(val);
		return s.isBlank() ? "N/A" : s;
	}

	/**
	 * Converts a stored country value to its full display name for the PDS report.
	 * The form previously stored 3-letter ISO codes (e.g., "ARM"); now it stores full names.
	 * This helper handles both old codes and new full-name values transparently.
	 */
	private static String getCountryName(String val) {
		if (val == null || val.isBlank()) return "";
		// If the value is already a full name (not a short code), return it directly.
		if (val.length() > 4) return val;
		// Legacy 2-4 letter ISO code lookup.
		switch (val.toUpperCase()) {
			case "AFG": return "Afghanistan";
			case "ALA": return "Aland Islands";
			case "ALB": return "Albania";
			case "DZA": return "Algeria";
			case "ASM": return "American Samoa";
			case "AND": return "Andorra";
			case "AGO": return "Angola";
			case "AIA": return "Anguilla";
			case "ATA": return "Antarctica";
			case "ATG": return "Antigua and Barbuda";
			case "ARG": return "Argentina";
			case "ARM": return "Armenia";
			case "ABW": return "Aruba";
			case "AUS": return "Australia";
			case "AUT": return "Austria";
			case "AZE": return "Azerbaijan";
			case "BHS": return "Bahamas";
			case "BHR": return "Bahrain";
			case "BGD": return "Bangladesh";
			case "BRB": return "Barbados";
			case "BLR": return "Belarus";
			case "BEL": return "Belgium";
			case "BLZ": return "Belize";
			case "BEN": return "Benin";
			case "BMU": return "Bermuda";
			case "BTN": return "Bhutan";
			case "BOL": return "Bolivia";
			case "BIH": return "Bosnia and Herzegovina";
			case "BWA": return "Botswana";
			case "BVT": return "Bouvet Island";
			case "BRA": return "Brazil";
			case "IOT": return "British Indian Ocean Territory";
			case "BRN": return "Brunei";
			case "BGR": return "Bulgaria";
			case "BFA": return "Burkina Faso";
			case "BDI": return "Burundi";
			case "CPV": return "Cabo Verde";
			case "KHM": return "Cambodia";
			case "CMR": return "Cameroon";
			case "CAN": return "Canada";
			case "CYM": return "Cayman Islands";
			case "CAF": return "Central African Republic";
			case "TCD": return "Chad";
			case "CHL": return "Chile";
			case "CHN": return "China";
			case "CXR": return "Christmas Island";
			case "CCK": return "Cocos Islands";
			case "COL": return "Colombia";
			case "COM": return "Comoros";
			case "COD": return "Congo (DRC)";
			case "COG": return "Congo (Republic)";
			case "COK": return "Cook Islands";
			case "CRI": return "Costa Rica";
			case "CIV": return "Cote d'Ivoire";
			case "HRV": return "Croatia";
			case "CUB": return "Cuba";
			case "CUW": return "Curacao";
			case "CYP": return "Cyprus";
			case "CZE": return "Czech Republic";
			case "DNK": return "Denmark";
			case "DJI": return "Djibouti";
			case "DMA": return "Dominica";
			case "DOM": return "Dominican Republic";
			case "ECU": return "Ecuador";
			case "EGY": return "Egypt";
			case "SLV": return "El Salvador";
			case "GNQ": return "Equatorial Guinea";
			case "ERI": return "Eritrea";
			case "EST": return "Estonia";
			case "SWZ": return "Eswatini";
			case "ETH": return "Ethiopia";
			case "FLK": return "Falkland Islands";
			case "FRO": return "Faroe Islands";
			case "FJI": return "Fiji";
			case "FIN": return "Finland";
			case "FRA": return "France";
			case "GUF": return "French Guiana";
			case "PYF": return "French Polynesia";
			case "ATF": return "French Southern Territories";
			case "GAB": return "Gabon";
			case "GMB": return "Gambia";
			case "GEO": return "Georgia";
			case "DEU": return "Germany";
			case "GHA": return "Ghana";
			case "GIB": return "Gibraltar";
			case "GRC": return "Greece";
			case "GRL": return "Greenland";
			case "GRD": return "Grenada";
			case "GLP": return "Guadeloupe";
			case "GUM": return "Guam";
			case "GTM": return "Guatemala";
			case "GGY": return "Guernsey";
			case "GIN": return "Guinea";
			case "GNB": return "Guinea-Bissau";
			case "GUY": return "Guyana";
			case "HTI": return "Haiti";
			case "HMD": return "Heard Island";
			case "VAT": return "Holy See";
			case "HND": return "Honduras";
			case "HKG": return "Hong Kong";
			case "HUN": return "Hungary";
			case "ISL": return "Iceland";
			case "IND": return "India";
			case "IDN": return "Indonesia";
			case "IRN": return "Iran";
			case "IRQ": return "Iraq";
			case "IRL": return "Ireland";
			case "IMN": return "Isle of Man";
			case "ISR": return "Israel";
			case "ITA": return "Italy";
			case "JAM": return "Jamaica";
			case "JPN": return "Japan";
			case "JEY": return "Jersey";
			case "JOR": return "Jordan";
			case "KAZ": return "Kazakhstan";
			case "KEN": return "Kenya";
			case "KIR": return "Kiribati";
			case "PRK": return "Korea (North)";
			case "KOR": return "Korea (South)";
			case "KWT": return "Kuwait";
			case "KGZ": return "Kyrgyzstan";
			case "LAO": return "Laos";
			case "LVA": return "Latvia";
			case "LBN": return "Lebanon";
			case "LSO": return "Lesotho";
			case "LBR": return "Liberia";
			case "LBY": return "Libya";
			case "LIE": return "Liechtenstein";
			case "LTU": return "Lithuania";
			case "LUX": return "Luxembourg";
			case "MAC": return "Macao";
			case "MDG": return "Madagascar";
			case "MWI": return "Malawi";
			case "MYS": return "Malaysia";
			case "MDV": return "Maldives";
			case "MLI": return "Mali";
			case "MLT": return "Malta";
			case "MHL": return "Marshall Islands";
			case "MTQ": return "Martinique";
			case "MRT": return "Mauritania";
			case "MUS": return "Mauritius";
			case "MYT": return "Mayotte";
			case "MEX": return "Mexico";
			case "FSM": return "Micronesia";
			case "MDA": return "Moldova";
			case "MCO": return "Monaco";
			case "MNG": return "Mongolia";
			case "MNE": return "Montenegro";
			case "MSR": return "Montserrat";
			case "MAR": return "Morocco";
			case "MOZ": return "Mozambique";
			case "MMR": return "Myanmar";
			case "NAM": return "Namibia";
			case "NRU": return "Nauru";
			case "NPL": return "Nepal";
			case "NLD": return "Netherlands";
			case "NCL": return "New Caledonia";
			case "NZL": return "New Zealand";
			case "NIC": return "Nicaragua";
			case "NER": return "Niger";
			case "NGA": return "Nigeria";
			case "NIU": return "Niue";
			case "NFK": return "Norfolk Island";
			case "MKD": return "North Macedonia";
			case "MNP": return "Northern Mariana Islands";
			case "NOR": return "Norway";
			case "OMN": return "Oman";
			case "PAK": return "Pakistan";
			case "PLW": return "Palau";
			case "PSE": return "Palestine";
			case "PAN": return "Panama";
			case "PNG": return "Papua New Guinea";
			case "PRY": return "Paraguay";
			case "PER": return "Peru";
			case "PHL": return "Philippines";
			case "PCN": return "Pitcairn";
			case "POL": return "Poland";
			case "PRT": return "Portugal";
			case "PRI": return "Puerto Rico";
			case "QAT": return "Qatar";
			case "REU": return "Reunion";
			case "ROU": return "Romania";
			case "RUS": return "Russia";
			case "RWA": return "Rwanda";
			case "BLM": return "Saint Barthelemy";
			case "SHN": return "Saint Helena";
			case "KNA": return "Saint Kitts and Nevis";
			case "LCA": return "Saint Lucia";
			case "MAF": return "Saint Martin";
			case "SPM": return "Saint Pierre and Miquelon";
			case "VCT": return "Saint Vincent and the Grenadines";
			case "WSM": return "Samoa";
			case "SMR": return "San Marino";
			case "STP": return "Sao Tome and Principe";
			case "SAU": return "Saudi Arabia";
			case "SEN": return "Senegal";
			case "SRB": return "Serbia";
			case "SYC": return "Seychelles";
			case "SLE": return "Sierra Leone";
			case "SGP": return "Singapore";
			case "SXM": return "Sint Maarten";
			case "SVK": return "Slovakia";
			case "SVN": return "Slovenia";
			case "SLB": return "Solomon Islands";
			case "SOM": return "Somalia";
			case "ZAF": return "South Africa";
			case "SGS": return "South Georgia";
			case "SSD": return "South Sudan";
			case "ESP": return "Spain";
			case "LKA": return "Sri Lanka";
			case "SDN": return "Sudan";
			case "SUR": return "Suriname";
			case "SJM": return "Svalbard and Jan Mayen";
			case "SWE": return "Sweden";
			case "CHE": return "Switzerland";
			case "SYR": return "Syria";
			case "TWN": return "Taiwan";
			case "TJK": return "Tajikistan";
			case "TZA": return "Tanzania";
			case "THA": return "Thailand";
			case "TLS": return "Timor-Leste";
			case "TGO": return "Togo";
			case "TKL": return "Tokelau";
			case "TON": return "Tonga";
			case "TTO": return "Trinidad and Tobago";
			case "TUN": return "Tunisia";
			case "TUR": return "Turkey";
			case "TKM": return "Turkmenistan";
			case "TCA": return "Turks and Caicos Islands";
			case "TUV": return "Tuvalu";
			case "UGA": return "Uganda";
			case "UKR": return "Ukraine";
			case "ARE": return "United Arab Emirates";
			case "GBR": return "United Kingdom";
			case "USA": return "United States";
			case "UMI": return "US Minor Outlying Islands";
			case "URY": return "Uruguay";
			case "UZB": return "Uzbekistan";
			case "VUT": return "Vanuatu";
			case "VEN": return "Venezuela";
			case "VNM": return "Vietnam";
			case "VGB": return "Virgin Islands (British)";
			case "VIR": return "Virgin Islands (US)";
			case "WLF": return "Wallis and Futuna";
			case "ESH": return "Western Sahara";
			case "YEM": return "Yemen";
			case "ZMB": return "Zambia";
			case "ZWE": return "Zimbabwe";
			default: return val; // unknown code or already a full name
		}
	}
	
	private static String getStringValueProvince(String val) {
		Map<String, String> provinceMap = generateProvinceMap();
		
		if(val != null) {
			if("null".equalsIgnoreCase(val.trim())) {
				return "";
			} else {
				return provinceMap.get(val);
			}
		} else {
			return "";
		}		
	}
	
	//Convert Amount to Words
	// Array for numbers less than 20
    private static final String[] belowTwenty = {
            "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
            "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
            "Seventeen", "Eighteen", "Nineteen"
    };

    // Array for tens
    private static final String[] tens = {
            "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    // Array for big numbers
    private static final String[] bigNumbers = {
            "", "Thousand", "Million", "Billion"
    };

    // Convert a number less than 1000 to words
    private static String convertLessThanThousand(int num) {
        String current;

        if (num % 100 < 20) {
            current = belowTwenty[num % 100];
            num /= 100;
        } else {
            current = belowTwenty[num % 10];
            num /= 10;

            current = tens[num % 10] + (current.isEmpty() ? "" : " " + current);
            num /= 10;
        }

        if (num == 0) return current;
        return belowTwenty[num] + " Hundred" + (current.isEmpty() ? "" : " and " + current);
    }

    // Convert the integer part to words
    private static String convertIntegerPart(int num) {
        if (num == 0) return "Zero";

        String prefix = "";
        if (num < 0) {
            num = -num;
            prefix = "Negative ";
        }

        String current = "";
        int place = 0;

        do {
            int n = num % 1000;
            if (n != 0) {
                String s = convertLessThanThousand(n);
                current = s + (bigNumbers[place].isEmpty() ? "" : " " + bigNumbers[place]) + (current.isEmpty() ? "" : " " + current);
            }
            place++;
            num /= 1000;
        } while (num > 0);

        return prefix + current.trim();
    }

    // Convert the fractional part to words
    private static String convertFractionalPart(int num) {
        return convertLessThanThousand(num);
    }

    // Convert the entire amount to words
    public static String convertAmountInWords(double amount) {
        int integerPart = (int) amount;
        int fractionalPart = (int) Math.round((amount - integerPart) * 100);

        String integerPartInWords = convertIntegerPart(integerPart);
        String formattedAmount = new DecimalFormat("#,###,###.00").format(amount);

        if (fractionalPart == 0) {
            return String.format("%s (%s)", integerPartInWords, formattedAmount);
        } else {
            String fractionalPartInWords = convertFractionalPart(fractionalPart);
            return String.format("%s and %s/100 (%s)", integerPartInWords, fractionalPartInWords, formattedAmount);
        }
    }

    public static void main(String[] args) {
        double amount1 = 12345.67;
        double amount2 = 12345.00;
        System.out.println("Amount in words: " + convertAmountInWords(amount1)); // Should include fractional part and formatted amount
        System.out.println("Amount in words: " + convertAmountInWords(amount2)); // Should exclude fractional part but include formatted amount
    }

}
