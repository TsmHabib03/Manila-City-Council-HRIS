package com.ian.web.systemsettings.profession;

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
import com.ian.web.systemsettings.degree_courses.DegreeCourses;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ProfessionController {

    private final ProfessionRepository professionRepository;

    @GetMapping("/professions")
    public String getData(Model model) {
        Iterable<Profession> listOfProfession = professionRepository.findAll();
        model.addAttribute("listOfProfession",listOfProfession);
        model.addAttribute("profession", new Profession());
        return "system-settings/profession/profession-list";
    }

    @PostMapping("/save-profession")
	@Transactional
	public String getRecord(
			@Valid Profession profession
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/profession/profession-list";
		}

		if(!Objects.isNull(profession.getId())){
			Profession professionModel = professionRepository.findById(profession.getId()).get();
			profession.setActive(professionModel.isActive());
		}
        profession.setProfessionName(profession.getProfessionName().toUpperCase());
		professionRepository.save(profession);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/professions";
	}

	@PostMapping("/update-profession-status/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect) {
		Profession profession = professionRepository.findById(id).get();

		profession.setActive(!profession.isActive());

		professionRepository.save(profession);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/professions";
	}
}