package com.ian.web.systemsettings.learning_type;

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
public class LearningTypeController {

     private final LearningTypeRepository learningTypeRepository;

    @GetMapping("/learning-type")
    public String getData(Model model) {
        model.addAttribute("listOfLearningDevelopment",learningTypeRepository.findAll());
        model.addAttribute("learningDevelopment", new LearningType());

        return "system-settings/learning-development/learning-development-list";
    }

    @PostMapping("/save-learning-type")
	@Transactional
	public String getRecord(
			@Valid LearningType learningType
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			model.addAttribute("listOfLearningDevelopment",learningTypeRepository.findAll());
            model.addAttribute("learningDevelopment", new LearningType());

            return "system-settings/learning-development/learning-development-list";
		}
		
		if(!Objects.isNull(learningType.getId())){
			LearningType learningTypeModel = learningTypeRepository.findById(learningType.getId()).get();
			learningType.setActive(learningTypeModel.isActive());
		}
        learningType.setTypeName(learningType.getTypeName().toUpperCase());
		learningTypeRepository.save(learningType);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/learning-type";
	}
}
