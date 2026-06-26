package com.ian.web.employee.learning;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

import org.apache.commons.beanutils.BeanUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.PdsCountDto;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.familybg.FamilyBgRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperience;
import com.ian.web.employee.workexperience.WorkExperienceRepository;
import com.ian.web.systemsettings.learning_type.LearningType;
import com.ian.web.systemsettings.learning_type.LearningTypeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LearningAndDevelopmentController {
    
    private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
    private final EmployeeRepository employeeRepository;
    private final LearningTypeRepository learningTypeRepository;
    
    private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;

	@GetMapping("/employee/learning-development/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/learning-development/{employeeId}/{empHashCode}", "/employee/learning-development/{employeeId}/{empHashCode}"})
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
			
			List<LearningAndDevelopment> learningDevelopmentList = learningAndDevelopmentRepository.findByEmployeeId(employeeId);
			model.addAttribute("learningDevelopmentList", learningDevelopmentList);
			
            model.addAttribute("learningTypeList", learningTypeRepository.findAll());

			LearningAndDevelopment learningAndDevelopment = new LearningAndDevelopment();
			learningAndDevelopment.setEmployee(employee);
			learningAndDevelopment.setShowMode(showMode);
			model.addAttribute("learningAndDevelopment", learningAndDevelopment );
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/learning-development";
		
	}

    @PostMapping({"/addLearningDevelopment", "/editLearningDevelopment"})
	public String saveFamilyBg(
			@Valid LearningAndDevelopment learningAndDevelopment
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("learningDevelopmentList", learningAndDevelopmentRepository.findByEmployeeId(learningAndDevelopment.getEmployee().getId()));
			return "employee/pds/learning-development";
		} 		
		
		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == learningAndDevelopment.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = learningAndDevelopment.getShowMode();
		learningAndDevelopment = learningAndDevelopmentRepository.save(learningAndDevelopment);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/employee/learning-development/"+learningAndDevelopment.getEmployee().getId()+"/"+showMode+"/"+learningAndDevelopment.getEmployee().getEmpHashCode();
	}

	@PostMapping("/deleteAllLearningDevelopment/{employeeId}")
	@Transactional
	public String deleteAllLearningDevelopment(
			@PathVariable long employeeId,
			@RequestParam(required = false) String showMode,
			@RequestParam String empHashCode,
			final RedirectAttributes redirect,
			HttpServletRequest request) {

		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == employeeId;
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		learningAndDevelopmentRepository.deleteAll(learningAndDevelopmentRepository.findByEmployeeId(employeeId));

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "All learning & development records removed."));
		return "redirect:/employee/learning-development/" + employeeId + "/" + (showMode == null ? "" : showMode) + "/" + empHashCode;
	}


}
