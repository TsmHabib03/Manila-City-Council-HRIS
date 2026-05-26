package com.ian.web.systemsettings.schools;

import java.util.Objects;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class SchoolController {
    
    private final SchoolRepository schoolRepository;

    @GetMapping("/schools")
    public String getData(Model model) {
        Iterable<School> listOfSchool = schoolRepository.findAll();
        model.addAttribute("listOfSchool",listOfSchool);
        model.addAttribute("school", new School());
        return "system-settings/school/school-list";
    }

    @PostMapping("/save-school")
	@Transactional
	public String getRecord(
			@Valid School school
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/school/school-list";
		}
		
		if(!Objects.isNull(school.getId())){
			School schoolModel = schoolRepository.findById(school.getId()).get();
			school.setActive(schoolModel.isActive());
		}
        school.setSchoolName(school.getSchoolName().toUpperCase());
		schoolRepository.save(school);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/schools";
	}

	@PostMapping("/update-school-status/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect) {
		School school = schoolRepository.findById(id).get();

		school.setActive(!school.isActive());

		schoolRepository.save(school);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/schools";
	}

}
