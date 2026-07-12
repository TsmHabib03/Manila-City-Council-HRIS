package com.ian.web.systemsettings.schools;

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
public class SchoolController {

    private final SchoolRepository schoolRepository;
    private final EducationalBackgroundRepository educationalBackgroundRepository;

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

	@PostMapping("/delete-school/{id}")
	public String deleteSchool(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/schools";
		}
		if (educationalBackgroundRepository.existsBySchoolId(id)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
				"Cannot delete this School. It is still assigned to one or more educational background records."));
			return "redirect:/schools";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> schoolRepository.deleteById(id), "School"));
		return "redirect:/schools";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}

}
