package com.ian.web.systemsettings.division;

import javax.transaction.Transactional;
import javax.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DivisionController {
	
	private final DivisionRepository divisionRepository;
	private final EmployeeRepository employeeRepository;
	
	@GetMapping("/divisions")
    public String getAllDivisions(Model model) {
        Iterable<Division> divisionList = divisionRepository.findAll();
        Iterable<Employee> employeeList = employeeRepository.findAll();
        model.addAttribute("employeeList", employeeList);
        model.addAttribute("divisionList", divisionList);
		model.addAttribute("division", new Division());
        return "system-settings/division/division-list";
    }

    @PostMapping("/save-division")
	@Transactional
	public String saveRecord(
			@Valid Division division
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/division/division-list";
		}
		
		division.setDivisionName(division.getDivisionName().toUpperCase());
		divisionRepository.save(division);		
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/divisions";
	}

}
