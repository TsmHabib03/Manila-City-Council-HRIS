package com.ian.web.systemsettings.academichonors;

import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.educationalbg.EducationalBackgroundRepository;
import com.ian.web.systemsettings.common.SettingsDeleteUtil;

@Controller
public class AcademicHonorsController {

	private AcademicHonorsRepository academicHonorsRepository;
	private EducationalBackgroundRepository educationalBackgroundRepository;

	public AcademicHonorsController(AcademicHonorsRepository academicHonorsRepository, EducationalBackgroundRepository educationalBackgroundRepository) {
        this.academicHonorsRepository = academicHonorsRepository;
        this.educationalBackgroundRepository = educationalBackgroundRepository;
    }
	
	@GetMapping("/academic-honors")
	public String listAll(Model model) {
		Iterable<AcademicHonors> academicHonors = academicHonorsRepository.findAll();
		model.addAttribute("academicHonorsList", academicHonors);
		model.addAttribute("academicHonors", new AcademicHonors());
		return "system-settings/academic-honors/academic-honors-list";
	}
	
	@PostMapping("/save-academic-honors")
	@Transactional
	public String saveAcademicHonors(
			@Valid AcademicHonors academicHonors
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/academic-honors/academic-honors-list";
		}
		
		academicHonors.setAcademicHonorsName(academicHonors.getAcademicHonorsName().toUpperCase());
		academicHonorsRepository.save(academicHonors);		
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/academic-honors";
	}

	@PostMapping("/delete-academic-honors/{id}")
	public String deleteAcademicHonors(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/academic-honors";
		}
		if (educationalBackgroundRepository.existsByAcademicHonorsId(id)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
				"Cannot delete this Academic Honors. It is still assigned to one or more educational background records."));
			return "redirect:/academic-honors";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> academicHonorsRepository.deleteById(id), "Academic Honors"));
		return "redirect:/academic-honors";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}

}
