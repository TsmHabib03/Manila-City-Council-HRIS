package com.ian.web.employee.otherinfo;

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
import com.ian.web.employee.otherinfoquestion.OtherInfoQuestionRepository;
import com.ian.web.employee.references.EmpReferencesRepository;
import com.ian.web.employee.voluntary_workexperience.VoluntaryWorkRepository;
import com.ian.web.employee.workexperience.WorkExperienceRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class OtherInfoController {

    private final OtherInfoRepository otherInfoRepository;
    private final EmployeeRepository employeeRepository;
    
    private final FamilyBgRepository familyBgRepository;
	private final EducationalBackgroundRepository educationalBackgroundRepository;
	private final CivilServiceEligibilityRepository civilServiceEligibilityRepository;
	private final WorkExperienceRepository workExperienceRepository;
	private final VoluntaryWorkRepository voluntaryWorkRepository;
	private final LearningAndDevelopmentRepository learningAndDevelopmentRepository;
	private final OtherInfoQuestionRepository otherInfoQuestionRepository;
	private final EmpReferencesRepository empReferencesRepository;
	private final GovermentIssuedIdRepository govermentIssuedIdRepository;

	@GetMapping("/employee/other-info/{employeeId}/{showMode}/{empHashCode}")
    //@GetMapping({"/profile/other-info/{employeeId}/{empHashCode}", "/employee/other-info/{employeeId}/{empHashCode}"})
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
			
			List<OtherInfo> otherInfoList = otherInfoRepository.findByEmployeeId(employeeId);
			model.addAttribute("otherInfoList", otherInfoList);

			OtherInfo otherInfo = new OtherInfo();
			otherInfo.setEmployee(employee);
			otherInfo.setShowMode(showMode);
			model.addAttribute("otherInfo", otherInfo );
						
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}		
		
		return "employee/pds/other-info";
		
	}

    @PostMapping({"/addOtherInfo", "/editOtherInfo"})
    @Transactional
	public String getRecord(
			@Valid OtherInfo otherInfo
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) throws IllegalAccessException, InvocationTargetException {
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("otherInfoList", otherInfoRepository.findByEmployeeId(otherInfo.getEmployee().getId()));
            return "employee/pds/other-info";
		}
		
		// Ownership check
		Employee actorObj = (Employee) request.getSession().getAttribute("actorObj");
		boolean isAdmin = actorObj != null && "ROLE_ADMIN".equals(actorObj.getUserType());
		boolean isOwnRecord = actorObj != null && actorObj.getId() == otherInfo.getEmployee().getId();
		if (!isAdmin && !isOwnRecord) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Access denied."));
			return "redirect:/dashboard";
		}

		String showMode = otherInfo.getShowMode();
		otherInfo = otherInfoRepository.save(otherInfo);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully saved."));
		return "redirect:/employee/other-info/"+otherInfo.getEmployee().getId()+"/"+showMode+"/"+otherInfo.getEmployee().getEmpHashCode();

	}

	@PostMapping("/deleteOtherInfo/{id}")
	@Transactional
	public String deleteOtherInfo(
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

		String redirectBase = "redirect:/employee/other-info/" + employeeId + "/" + (showMode == null ? "" : showMode) + "/" + empHashCode;

		Optional<OtherInfo> recordOptional = otherInfoRepository.findById(id);
		if (!recordOptional.isPresent() || recordOptional.get().getEmployee().getId() != employeeId) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Record not found."));
			return redirectBase;
		}

		otherInfoRepository.delete(recordOptional.get());

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record successfully deleted."));
		return redirectBase;
	}
}
