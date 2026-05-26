package com.ian.web.systemsettings.levels;

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
public class LevelController {
    
    private final LevelRepository levelRepository;

    @GetMapping("/levels")
    public String getData(Model model) {
        Iterable<Level> listOfLevels = levelRepository.findAll();
        model.addAttribute("listOfLevels",listOfLevels);
        model.addAttribute("level", new Level());
        return "system-settings/level/level-list";
    }

    @PostMapping("/save-level")
	@Transactional
	public String getRecord(
			@Valid Level level
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/level/level-list";
		}
		
		if(!Objects.isNull(level.getId())){
			Level levelModel = levelRepository.findById(level.getId()).get();
			level.setActive(levelModel.isActive());
		}
        level.setLevelName(level.getLevelName().toUpperCase());
		levelRepository.save(level);

		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/levels";
	}

	@PostMapping("/update-level/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect) {
		Level level = levelRepository.findById(id).get();

		level.setActive(!level.isActive());

	    levelRepository.save(level);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/levels";
	}

}
