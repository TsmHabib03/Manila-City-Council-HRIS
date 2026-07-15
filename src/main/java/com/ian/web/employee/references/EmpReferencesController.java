package com.ian.web.employee.references;

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
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmpReferencesController {

    private final EmpReferencesRepository empReferencesRepository;
    private final EmployeeRepository employeeRepository;
    
    private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoRepository otherInfoRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;

	@GetMapping("/employee/references/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/references/{employeeId}/{empHashCode}", "/employee/references/{employeeId}/{empHashCode}"})
	public String getRecords(Model model, @PathVariable long employeeId, @PathVariable String showMode, @PathVariable String empHashCode, HttpServletRequest request) {
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
			
			List<EmpReferences> referencesList = empReferencesRepository.findByEmployeeId(employeeId);
			model.addAttribute("referencesList", referencesList);

			EmpReferences references = new EmpReferences();
			references.setEmployee(employee);
			references.setShowMode(showMode);
			model.addAttribute("references", references );
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/references";
		
	}

    @PostMapping({"/addReferences", "/editReferences"})
    @Transactional
	public String getRecord(
			@Valid EmpReferences references
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("referencesList", empReferencesRepository.findByEmployeeId(references.getEmployee().getId()));
            return "employee/pds/references";
		}
		
		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == references.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = references.getShowMode();
		references = empReferencesRepository.save(references);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully saved."));
		return "redirect:/employee/references/"+references.getEmployee().getId()+"/"+showMode+"/"+references.getEmployee().getEmpHashCode();

	}

	@PostMapping("/deleteReferences/{id}")
	@Transactional
	public String deleteReferences(
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

		String redirectBase = "redirect:/employee/references/" + employeeId + "/" + (showMode == null ? "" : showMode) + "/" + empHashCode;

		Optional<EmpReferences> recordOptional = empReferencesRepository.findById(id);
		if (!recordOptional.isPresent() || recordOptional.get().getEmployee().getId() != employeeId) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Record not found."));
			return redirectBase;
		}

		empReferencesRepository.delete(recordOptional.get());

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record successfully deleted."));
		return redirectBase;
	}

}
