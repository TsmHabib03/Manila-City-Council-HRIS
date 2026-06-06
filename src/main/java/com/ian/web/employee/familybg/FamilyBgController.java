package com.ian.web.employee.familybg;

import java.beans.PropertyEditorSupport;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.PdsCountDto;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.employee.eligibility.CivilServiceEligibilityRepository;
import com.ian.web.employee.govermentid.GovermentIssuedIdRepository;
import com.ian.web.employee.learning.LearningAndDevelopmentRepository;
import com.ian.web.employee.otherinfo.OtherInfoRepository;
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class FamilyBgController {
	
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
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder) {
		binder.registerCustomEditor(LocalDate.class, new PropertyEditorSupport() {
			@Override
			public void setAsText(String text) throws IllegalArgumentException {
				if (text == null || text.trim().isEmpty()) {
					setValue(null);
				} else {
					setValue(LocalDate.parse(text.trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd")));
				}
			}
		});
	}

	@GetMapping("/employee/familybg/{employeeId}/{showMode}/{empHashCode}")
	//@GetMapping({"/profile/familybg/{employeeId}/{empHashCode}", "/employee/familybg/{employeeId}/{empHashCode}"})
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
			
			List<FamilyBg> familyBgList = familyBgRepository.findByEmployeeId(employeeId);
			model.addAttribute("familyBgList", familyBgList);
			
			FamilyBg familyBg = new FamilyBg();
			familyBg.setEmployee(employee);
			familyBg.setShowMode(showMode);
			model.addAttribute("familyBg", familyBg );
						
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/family-background";
		
	}
	
	@PostMapping({"/addFamilyBg", "/editFamilyBg"})
	public String saveFamilyBg(
			@Valid FamilyBg familyBg
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {

	    String uxMessageText = "Record added successfully.";
	    String showModeEarly = familyBg.getShowMode() != null ? familyBg.getShowMode() : "HRADMIN";
	    String redirectBase = "redirect:/employee/familybg/"
	            + familyBg.getEmployee().getId() + "/" + showModeEarly + "/" + familyBg.getEmployee().getEmpHashCode();

		if (errors.hasErrors()) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			return redirectBase;
		} else {
			if(familyBg.getId() == 0 && !"CHILDREN".equalsIgnoreCase(familyBg.getRelationship())) {
				FamilyBg recordMatch = familyBgRepository.findByEmployeeIdAndRelationship(familyBg.getEmployee().getId(), familyBg.getRelationship());
				if (recordMatch != null) {
					redirect.addFlashAttribute("msg", new UXMessage("ERROR", "You already have a record for your " + familyBg.getRelationship()));
					return redirectBase;
				} else {
				    redirect.addFlashAttribute("msg", new UXMessage("SUCCESS", uxMessageText));
				}
			}
		}		
		
		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == familyBg.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = familyBg.getShowMode();
		familyBg = familyBgRepository.save(familyBg);
		
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/employee/familybg/"+familyBg.getEmployee().getId()+"/"+showMode+"/"+familyBg.getEmployee().getEmpHashCode();
	}

}
