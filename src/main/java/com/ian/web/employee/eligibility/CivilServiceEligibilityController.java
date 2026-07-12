package com.ian.web.employee.eligibility;

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
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.eligibility.EligibilityRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class CivilServiceEligibilityController {
    
    private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final EmployeeRepository employeeRepository;
	private final EligibilityRepository eligibilityRepository;
	
	private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;

	@GetMapping("/employee/civil-eligibility/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/civil-eligibility/{employeeId}/{empHashCode}", "/employee/civil-eligibility/{employeeId}/{empHashCode}"})
	public String viewEmployee(Model model, @PathVariable long employeeId, @PathVariable String showMode, @PathVariable String empHashCode, HttpServletRequest request) {
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
			
			List<CivilServiceEligibility> civilEligibilityList = civilServiceEligibilityRepository.findByEmployeeId(employee.getId());
			model.addAttribute("civilEligibilityList", civilEligibilityList);
			model.addAttribute("eligibilityList", eligibilityRepository.findAll());
			
			CivilServiceEligibility civilServiceEligibility = new CivilServiceEligibility();
			civilServiceEligibility.setEmployee(employee);
			civilServiceEligibility.setShowMode(showMode);
			model.addAttribute("civilServiceEligibility", civilServiceEligibility );
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/eligibility";
		
	}
	
	@PostMapping({"/addCivilEligibility", "/editCivilEligibility"})
	public String saveFamilyBg(
			@Valid CivilServiceEligibility civilServiceEligibility
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			Long empId = civilServiceEligibility.getEmployee() != null
				? civilServiceEligibility.getEmployee().getId() : null;
			if (empId != null) {
				Employee employee = employeeRepository.findById(empId).orElse(null);
				if (employee != null) {
					employee.setShowMode(civilServiceEligibility.getShowMode());
					model.addAttribute("employee", employee);
				}
				model.addAttribute("civilEligibilityList",
					civilServiceEligibilityRepository.findByEmployeeId(empId));
			}
			model.addAttribute("eligibilityList", eligibilityRepository.findAll());
			model.addAttribute("civilServiceEligibility", civilServiceEligibility);
			return "employee/pds/eligibility";
		}

		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == civilServiceEligibility.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = civilServiceEligibility.getShowMode();
		Employee freshEmployee = employeeRepository
			.findById(civilServiceEligibility.getEmployee().getId())
			.orElseThrow();
		civilServiceEligibility.setEmployee(freshEmployee);
		civilServiceEligibility = civilServiceEligibilityRepository.save(civilServiceEligibility);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/employee/civil-eligibility/"+civilServiceEligibility.getEmployee().getId()+"/"+showMode+"/"+civilServiceEligibility.getEmployee().getEmpHashCode();
	}

}
