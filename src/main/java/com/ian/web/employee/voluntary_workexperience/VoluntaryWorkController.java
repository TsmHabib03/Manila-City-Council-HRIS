package com.ian.web.employee.voluntary_workexperience;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.transaction.annotation.Transactional;
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
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class VoluntaryWorkController {
    
    private final VoluntaryWorkRepository voluntaryWorkRepository;
    private final EmployeeRepository employeeRepository; 
    
    private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
     
     
	@GetMapping("/employee/voluntary-work/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/voluntary-work/{employeeId}/{empHashCode}", "/employee/voluntary-work/{employeeId}/{empHashCode}"})
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
			
			List<VoluntaryWork> voluntaryWorkList = voluntaryWorkRepository.findByEmployeeId(employeeId);
			model.addAttribute("voluntaryWorkList", voluntaryWorkList);
			
			VoluntaryWork voluntaryWork = new VoluntaryWork();
			voluntaryWork.setEmployee(employee);
			voluntaryWork.setShowMode(showMode);
			model.addAttribute("voluntaryWork", voluntaryWork);
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/voluntary-work";
		
	}
	
	@PostMapping({"/addVoluntaryWork", "/editVoluntaryWork"})
	public String saveFamilyBg(
			@Valid VoluntaryWork voluntaryWork
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    
		if (errors.hasErrors()) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			return "redirect:/employee/voluntary-work/"
				+ voluntaryWork.getEmployee().getId() + "/"
				+ voluntaryWork.getShowMode() + "/"
				+ voluntaryWork.getEmployee().getEmpHashCode();
		}	
		
		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == voluntaryWork.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = voluntaryWork.getShowMode();
		voluntaryWork = voluntaryWorkRepository.save(voluntaryWork);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully saved."));
		return "redirect:/employee/voluntary-work/"+voluntaryWork.getEmployee().getId()+"/"+showMode+"/"+voluntaryWork.getEmployee().getEmpHashCode();
	}

	@PostMapping("/deleteAllVoluntaryWork/{employeeId}")
	@Transactional
	public String deleteAllVoluntaryWork(
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

		voluntaryWorkRepository.deleteAll(voluntaryWorkRepository.findByEmployeeId(employeeId));

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "All voluntary work records removed."));
		return "redirect:/employee/voluntary-work/" + employeeId + "/" + (showMode == null ? "" : showMode) + "/" + empHashCode;
	}

}
