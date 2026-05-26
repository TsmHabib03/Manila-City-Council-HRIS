package com.ian.web.systemsettings.degreelevels;

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
public class DegreeLevelController {
    
    private final DegreeLevelRepository degreeLevelRepository;

    @GetMapping("/degree-levels")
    public String getAllDegreeLevels(Model model) {
        Iterable<DegreeLevel> degreeLevel = degreeLevelRepository.findAll();
        model.addAttribute("degreeLevelList", degreeLevel);
		model.addAttribute("degreeLevel", new DegreeLevel());
        return "system-settings/degree-levels/degree-levels-list";
    }

    @PostMapping("/save-degree-level")
	@Transactional
	public String saveRecord(
			@Valid DegreeLevel degreeLevel
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/degree-levels/degree-levels-list";
		}
		if(!Objects.isNull(degreeLevel.getId())){
			DegreeLevel degreeLevelModel = degreeLevelRepository.findById(degreeLevel.getId()).get();
			degreeLevel.setActive(degreeLevelModel.isActive());
		}
		degreeLevel.setDegreeName(degreeLevel.getDegreeName().toUpperCase());
		degreeLevelRepository.save(degreeLevel);		
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/degree-levels";
	}

	@PostMapping("/update-degree-level-status/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect, Model model) {
		DegreeLevel degreeLevel = degreeLevelRepository.findById(id).get();
		degreeLevel.setActive(!degreeLevel.isActive());
		degreeLevelRepository.save(degreeLevel);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/degree-levels";
	}
    
    
}
