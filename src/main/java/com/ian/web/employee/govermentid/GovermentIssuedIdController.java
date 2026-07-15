package com.ian.web.employee.govermentid;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;

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
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWork;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class GovermentIssuedIdController {

    private final GovermentIssuedIdRepository govermentIssuedIdRepository;
    private final EmployeeRepository employeeRepository;
    
    private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	
	@GetMapping("/employee/government-id/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/government-id/{employeeId}/{empHashCode}", "/employee/government-id/{employeeId}/{empHashCode}"})
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
			
			List<GovermentIssuedId> governmentIdList = govermentIssuedIdRepository.findByEmployeeId(employeeId);
			model.addAttribute("governmentIdList", governmentIdList);
			
			GovermentIssuedId govermentIssuedId = new GovermentIssuedId();
			govermentIssuedId.setEmployee(employee);
			govermentIssuedId.setShowMode(showMode);
			model.addAttribute("govermentIssuedId", govermentIssuedId);
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/gov-issued-id";
		
	}
	
	@PostMapping({"/addGovernmentId", "/editGovernmentId"})
	public String saveFamilyBg(
			@Valid GovermentIssuedId govermentIssuedId
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	     
	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("governmentIdList", govermentIssuedIdRepository.findByEmployeeId(govermentIssuedId.getEmployee().getId()));
			return "employee/pds/gov-issued-id";
		} 
		
		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == govermentIssuedId.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = govermentIssuedId.getShowMode();
		govermentIssuedId = govermentIssuedIdRepository.save(govermentIssuedId);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully saved."));
		return "redirect:/employee/government-id/"+govermentIssuedId.getEmployee().getId()+"/"+showMode+"/"+govermentIssuedId.getEmployee().getEmpHashCode();
	}

	@PostMapping("/deleteGovernmentId/{id}")
	@Transactional
	public String deleteGovernmentId(
			@PathVariable long id,
			@RequestParam long employeeId,
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

		String redirectBase = "redirect:/employee/government-id/" + employeeId + "/" + (showMode == null ? "" : showMode) + "/" + empHashCode;

		Optional<GovermentIssuedId> recordOptional = govermentIssuedIdRepository.findById(id);
		if (!recordOptional.isPresent() || recordOptional.get().getEmployee().getId() != employeeId) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Record not found."));
			return redirectBase;
		}

		govermentIssuedIdRepository.delete(recordOptional.get());

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record successfully deleted."));
		return redirectBase;
	}

}
