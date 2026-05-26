package com.ian.web.systemsettings.degree_courses;

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
public class DegreeCoursesController {
    private final DegreeCoursesRepository degreeCoursesRepository;

    @GetMapping("/degree-courses")
    public String getData(Model model) {
        Iterable<DegreeCourses> listOfDegreeCourses = degreeCoursesRepository.findAll();
        model.addAttribute("listOfDegreeCourses", listOfDegreeCourses);
        model.addAttribute("degreeCourse", new DegreeCourses());
        return "system-settings/degree-courses/degree-courses-list";
    }

    @PostMapping("/save-degree-courses")
	@Transactional
	public String getRecord(
			@Valid DegreeCourses degreeCourses
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {

		if (errors.hasErrors()) {
			System.out.println(errors);
			model.addAttribute("degreeCourse", degreeCourses);
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/degree-courses/degree-courses-list";
		}
		
		if(!Objects.isNull(degreeCourses.getId())){
			DegreeCourses degreeCoursesModel = degreeCoursesRepository.findById(degreeCourses.getId()).get();
			degreeCourses.setLawDegree(degreeCoursesModel.isLawDegree());
			degreeCourses.setActive(degreeCoursesModel.isActive());
		}
		degreeCourses.setDegreeCourseName(degreeCourses.getDegreeCourseName().toUpperCase());
		degreeCourses.setAbbreviation(degreeCourses.getAbbreviation().toUpperCase());
		degreeCoursesRepository.save(degreeCourses);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/degree-courses";
	}

	@PostMapping("/update-degree-courses-status/{action}/{id}")
	public String updateStatus(@PathVariable("id") Long id,@PathVariable("action") String action, final RedirectAttributes redirect) {
		DegreeCourses degreeCourses = degreeCoursesRepository.findById(id).get();

		if(action.equals("isActive")){
			degreeCourses.setActive(!degreeCourses.isActive());
		}else{ degreeCourses.setLawDegree(!degreeCourses.isLawDegree()); }

		degreeCoursesRepository.save(degreeCourses);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/degree-courses";
	}
    
}
