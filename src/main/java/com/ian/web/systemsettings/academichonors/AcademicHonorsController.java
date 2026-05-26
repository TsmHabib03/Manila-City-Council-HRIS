package com.ian.web.systemsettings.academichonors;

import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ian.web.common.model.UXMessage;

@Controller
public class AcademicHonorsController {
	
	private AcademicHonorsRepository academicHonorsRepository;
	
	public AcademicHonorsController(AcademicHonorsRepository academicHonorsRepository) {
        this.academicHonorsRepository = academicHonorsRepository;
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

}
