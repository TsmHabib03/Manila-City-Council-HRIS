package com.ian.web.systemsettings.scholarship;

import java.util.Objects;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ScholarshipController {

    private final ScholarshipRepository scholarshipRepository;
    private final EducationalBackgroundRepository educationalBackgroundRepository;

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

	@PostMapping("/delete-scholarship/{id}")
	public String deleteScholarship(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/scholarships";
		}
		if (educationalBackgroundRepository.existsByScholarshipId(id)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
				"Cannot delete this Scholarship. It is still assigned to one or more educational background records."));
			return "redirect:/scholarships";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> scholarshipRepository.deleteById(id), "Scholarship"));
		return "redirect:/scholarships";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}

}
