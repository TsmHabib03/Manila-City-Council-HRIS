package com.ian.web.systemsettings.scholarship;

import java.util.Objects;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipRepository scholarshipRepository;

    @GetMapping("/scholarships")
    public String getData(Model model) {
        Iterable<Scholarship> listOfScholarship = scholarshipRepository.findAll();
        model.addAttribute("listOfScholarship",listOfScholarship);
        model.addAttribute("scholarship", new Scholarship());
        return "system-settings/scholarship/scholarship-list";
    }

    @PostMapping("/save-scholarship")
	@Transactional
	public String getRecord(
			@Valid Scholarship scholarship
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/scholarship/scholarship-list";
		}
        scholarship.setScholarshipName(scholarship.getScholarshipName().toUpperCase());
		scholarshipRepository.save(scholarship);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/scholarships";
	}
    
}
