package com.ian.web.systemsettings.salary_grades;

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
public class SalaryGradeController {

    private final SalaryGradeRepository salaryGradeRepository;

    @GetMapping("/salary-grades")
    public String getData(Model model) {
        Iterable<SalaryGrade> listOfSalaryGrade = salaryGradeRepository.findAll();
        model.addAttribute("listOfSalaryGrade",listOfSalaryGrade);
        model.addAttribute("salaryGrade", new SalaryGrade());
        return "system-settings/salary-grade/salary-grade-list";
    }

    @PostMapping("/save-salary-grade")
	@Transactional
	public String getRecord(
			@Valid SalaryGrade salaryGrade
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/salary-grade/salary-grade-list";
		}
		
		if(!Objects.isNull(salaryGrade.getId())){
			SalaryGrade salaryGradeModel = salaryGradeRepository.findById(salaryGrade.getId()).get();
			salaryGrade.setActive(salaryGradeModel.isActive());
		}
        salaryGrade.setSalaryGradeGroup(salaryGrade.getSalaryGradeGroup().toUpperCase());
		salaryGrade.setSalaryGradeNumber((Integer) salaryGrade.getSalaryGradeNumber());
		salaryGradeRepository.save(salaryGrade);
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/salary-grades";
	}

	@PostMapping("/update-salary-grade-status/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect) {
		SalaryGrade salaryGrade = salaryGradeRepository.findById(id).get();

		salaryGrade.setActive(!salaryGrade.isActive());

		salaryGradeRepository.save(salaryGrade);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/salary-grades";
	}
    
}
