package com.ian.web.systemsettings.position_title;

import javax.servlet.http.HttpServletRequest;
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
public class PositionDescriptionFormController {
    
    // Repository for the PositionTitle entity (used for populating the dropdown)
    private final PositionTitleRepository positionTitleRepository;
    
    // Repository for the PositionDescriptionForm entity
    private final PositionDescriptionFormRepository positionDescriptionFormRepository;

    @GetMapping("/position-desc-form-report")
	public String viewPositionDescFormReport(Model model, HttpServletRequest request) {		
		model.addAttribute("positionDescriptionForm", new PositionDescriptionForm());
		model.addAttribute("positionTitleList", positionTitleRepository.findAll());
		model.addAttribute("positionDescriptionFormList", positionDescriptionFormRepository.findAll());
		return "system-settings/position-title/position-description";		
	}
    
    /**
     * Handles the submission of the Position Description Form.
     * Saves the new or updated record to the database.
     *
     * @param positionDescriptionForm The form object populated by Thymeleaf.
     * @param errors Validation errors, if any.
     * @param redirect Attributes for the redirect.
     * @param model The Spring model.
     * @return A redirect string to the main report/list page.
     */
    @PostMapping("/savePositionDescription")
	@Transactional
	public String savePositionDescription(
			@Valid PositionDescriptionForm positionDescriptionForm,
			Errors errors,
			final RedirectAttributes redirect,
			Model model
			) {

		// Basic error handling (can be expanded)
		if (errors.hasErrors()) {
            // Repopulate the list for the dropdown in case of an error
			model.addAttribute("positionTitleList", positionTitleRepository.findAll());
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check the form for errors."));
			// Return to the view that contains the modal to show the errors
			return "system-settings/position-title/reports/position-description";
		}
     
        // Save the form object to the database using the repository
        positionDescriptionFormRepository.save(positionDescriptionForm);
        
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/position-desc-form-report";
	}
}
