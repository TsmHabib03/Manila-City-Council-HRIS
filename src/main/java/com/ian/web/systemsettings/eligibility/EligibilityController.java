package com.ian.web.systemsettings.eligibility;

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
public class EligibilityController {
    
    private final EligibilityRepository eligibilityRepository;

    @GetMapping("/eligibility")
    public String getAllDivisions(Model model) {
        model.addAttribute("eligibilityList", eligibilityRepository.findAll());
		model.addAttribute("eligibility", new Eligibility());
        return "system-settings/eligibility/eligibility-list";
    }

    @PostMapping("/save-eligibility")
	@Transactional
	public String saveRecord(
			@Valid Eligibility eligibility
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
            model.addAttribute("listOfEligibility", eligibilityRepository.findAll());
		    model.addAttribute("eligibility", eligibility);
			return "system-settings/eligibility/eligibility-list";
		}
		
		eligibility.setEligibilityName(eligibility.getEligibilityName().toUpperCase());
		eligibilityRepository.save(eligibility);		
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/eligibility";
	}

}
