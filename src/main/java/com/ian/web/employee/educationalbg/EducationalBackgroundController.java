package com.ian.web.employee.educationalbg;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.PdsCountDto;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.academichonors.AcademicHonors;
import com.ian.web.systemsettings.academichonors.AcademicHonorsRepository;
import com.ian.web.systemsettings.degree_courses.DegreeCourses;
import com.ian.web.systemsettings.degree_courses.DegreeCoursesRepository;
import com.ian.web.systemsettings.degreelevels.DegreeLevelRepository;
import com.ian.web.systemsettings.scholarship.Scholarship;
import com.ian.web.systemsettings.scholarship.ScholarshipRepository;
import com.ian.web.systemsettings.schools.School;
import com.ian.web.systemsettings.schools.SchoolRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EducationalBackgroundController {
    
    private final EducationalBackgroundRepository educationalBackgroundRepository;
    private final DegreeLevelRepository degreeLevelRepository;
    private final SchoolRepository schoolRepository;
    private final DegreeCoursesRepository degreeCoursesRepository;
    private final ScholarshipRepository scholarshipRepository;
    private final AcademicHonorsRepository academicHonorsRepository;
    private final EmployeeRepository employeeRepository;
    
    private final FamilyBgRepository familyBgRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
    
	@GetMapping("/employee/educationalbg/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/educationalbg/{employeeId}/{empHashCode}", "/employee/educationalbg/{employeeId}/{empHashCode}"})
    public String getRecord(Model model, @PathVariable long employeeId, @PathVariable String showMode, @PathVariable String empHashCode, HttpServletRequest request){
    	Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {		
			
			Employee employee = optional.orElseGet(() -> new Employee());
			
			PdsCountDto pdsDtoCount = new PdsCountDto();
			pdsDtoCount.setFamilyBgCount(familyBgRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setEducationalBgCount(educationalBackgroundRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setEligibilityCount(civilServiceEligibilityRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setWorkExperienceCount(workExperienceRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setVoluntaryWorkCount(voluntaryWorkRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setLearningDevCount(learningAndDevelopmentRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setOtherInfoCount(otherInfoRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setOtherInfoQuestionsCount(otherInfoQuestionRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setReferencesCount(empReferencesRepository.findByEmployeeId(employeeId).size());
			pdsDtoCount.setGovIdCount(govermentIssuedIdRepository.findByEmployeeId(employeeId).size());
			
			employee.setPdsCountDto(pdsDtoCount);
			employee.setShowMode(showMode);
			
			model.addAttribute("employee", employee);
			
			List<EducationalBackground> educationalBgList = educationalBackgroundRepository.findByEmployeeId(employeeId);
			
			List<EducationalBackground> educationalBgListNew = new ArrayList<>();
			for(EducationalBackground obj : educationalBgList) {
				if(obj.getAcademicHonors() == null) {
					AcademicHonors a = new AcademicHonors();
					a.setId(0L);
					a.setAcademicHonorsName("");
					obj.setAcademicHonors(a);
				}
				
				if(obj.getScholarship() == null) {
					Scholarship s = new Scholarship();
					s.setId(0L);
					s.setScholarshipName("");
					obj.setScholarship(s);
				}
				
				if(obj.getDegreeCourse() == null) {
					DegreeCourses d = new DegreeCourses();
					d.setId(0L);
					d.setDegreeCourseName("");
					obj.setDegreeCourse(d);
				}
				
				educationalBgListNew.add(obj);
			}
			
			model.addAttribute("educationalBgList", educationalBgListNew);
			
			model.addAttribute("degreeLevelList", degreeLevelRepository.findAll());
			model.addAttribute("schoolList", schoolRepository.findAllByOrderBySchoolNameAsc());
			model.addAttribute("degreeCourseList", degreeCoursesRepository.findAllByOrderByDegreeCourseNameAsc());
			model.addAttribute("scholarshipList", scholarshipRepository.findAll());			
			model.addAttribute("academicHonorsList", academicHonorsRepository.findAll());
			
			EducationalBackground educationalBg = new EducationalBackground();
			educationalBg.setEmployee(employee);
			educationalBg.setShowMode(showMode);
			model.addAttribute("educationalBg", educationalBg );
			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/educational-background";
    }
    
    @PostMapping({"/addEducationalBg", "/editEducationalBg"})
	public String saveEducationalBg(
			@Valid EducationalBackground educBackground
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("educationalBgList", educationalBackgroundRepository.findByEmployeeId(educBackground.getEmployee().getId()));
			return "employee/pds/educational-background";
		}				
		
		if(educBackground.getDegreeCourse() == null) {
			educBackground.setDegreeCourse(null);
		} else if(educBackground.getDegreeCourse().getId() == null) {
			educBackground.setDegreeCourse(null);
		}
		
		if(educBackground.getAcademicHonors() == null) {
			educBackground.setAcademicHonors(null);
		} else if(educBackground.getAcademicHonors().getId() == null) {
			educBackground.setAcademicHonors(null);
		}
		
		if(educBackground.getScholarship() == null) {
			educBackground.setScholarship(null);
		} else if(educBackground.getScholarship().getId() == null) {
			educBackground.setScholarship(null);
		}

		// When "OTHER (type below)" is selected, school.id == 0.
		// Auto-create (or reuse) a School record from the custom name.
		boolean schoolIdIsAbsent = educBackground.getSchool() == null
				|| educBackground.getSchool().getId() == null
				|| educBackground.getSchool().getId() == 0L;
		if (schoolIdIsAbsent) {
			String customName = educBackground.getSchoolCustomName();
			if (customName != null && !customName.isBlank()) {
				School resolvedSchool = schoolRepository.findBySchoolName(customName.trim())
						.orElseGet(() -> schoolRepository.save(
								School.builder().schoolName(customName.trim()).isActive(true).build()));
				educBackground.setSchool(resolvedSchool);
			} else {
				educBackground.setSchool(null);
			}
		}

		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == educBackground.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = educBackground.getShowMode();
		educBackground = educationalBackgroundRepository.save(educBackground);
		
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/employee/educationalbg/"+educBackground.getEmployee().getId()+"/"+showMode+"/"+educBackground.getEmployee().getEmpHashCode();
	}
    

//    @GetMapping("/educational-background/{employeeId}")
//    public String getRecord(Model model, @PathVariable("employeeId") long id){
//        if(!employeeRepository.findById(id).isPresent()){
//            model.addAttribute("uxmessage", new UXMessage("ERROR", "Employee doesn't exist"));
//			return "redirect:/dashboard";
//        }
//        Employee employee = employeeRepository.findById(id).get();
//        model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
//        model.addAttribute("listOfSchools", schoolRepository.findAll());
//        model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
//        model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
//        model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
//        model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAllByEmployee(employee));
//
//        model.addAttribute("employee", employee);
//        model.addAttribute("educationalBackground", new EducationalBackgroundModel());
//
//        return "employee/pds/educ-background";
//    }

//    @PostMapping("/save-educational-background/{employeeId}")
//    @Transactional
//	public String getRecord(
//			@Valid EducationalBackgroundModel educationalBackgroundModel
//            ,@PathVariable("employeeId") long id
//			,Errors errors
//			,final RedirectAttributes redirect
//			,Model model
//			) throws IllegalAccessException, InvocationTargetException {
//		if (errors.hasErrors()) {
//			Employee employee = employeeRepository.findById(id).get();
//            model.addAttribute("listOfDegreeLevels", degreeLevelRepository.findAll());
//            model.addAttribute("listOfSchools", schoolRepository.findAll());
//            model.addAttribute("listOfDegreeCourses", degreeCoursesRepository.findAll());
//            model.addAttribute("listOfScholarships", scholarshipRepository.findAll());
//            model.addAttribute("listOfAcademicHonors", academicHonorsRepository.findAll());
//            model.addAttribute("listOfEducationalBackground", educationalBackgroundRepository.findAllByEmployee(employee));
//
//
//            model.addAttribute("employee", employeeRepository.findById(id).get());
//            model.addAttribute("educationalBackground", educationalBackgroundModel);
//			return "employee/pds/educ-background";
//		}
//
//        Employee employee = employeeRepository.findById(id).orElseGet(()->new Employee());
//        EducationalBackground educationalBackground = new EducationalBackground();
//        BeanUtils.copyProperties(educationalBackground, educationalBackgroundModel);
////        educationalBackground.setDegreeLevel(degreeLevelRepository.findById(educationalBackgroundModel.getDegreeLevelId()).get());
////        educationalBackground.setSchool(schoolRepository.findById(educationalBackgroundModel.getSchoolId()).get());
////        educationalBackground.setDegreeCourse(degreeCoursesRepository.findById(educationalBackgroundModel.getDegreeCourseId()).get());
////        educationalBackground.setScholarship(scholarshipRepository.findById(educationalBackgroundModel.getScholarshipId()).get());
////        educationalBackground.setAcademicHonors(academicHonorsRepository.findById(educationalBackgroundModel.getAcademicHonorsId()).get());
//        educationalBackground.setEmployee(employee);
//
////        List<EducationalBackground> listOfEmployeeEducationalBackground = employee.getEducationalBackgrounds();
////        listOfEmployeeEducationalBackground.add(educationalBackground);
////        employee.setEducationalBackgrounds(listOfEmployeeEducationalBackground);
//        employeeRepository.save(employee);
//
//		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
//		return "redirect:/educational-background/"+id;
//	}

}
