package com.ian.web.systemsettings.employee_status;

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
public class EmployeeStatusController {
    
    private final EmployeeStatusRepository employeeStatusRepository;

    @GetMapping("/employee-status")
    public String getData(Model model) {
        Iterable<EmployeeStatus> listOfEmployeeStatus = employeeStatusRepository.findAll();
        model.addAttribute("listOfEmployeeStatus",listOfEmployeeStatus);
        model.addAttribute("employeeStatus", new EmployeeStatus());
        return "system-settings/employee-status/employee-status-list";
    }

    @PostMapping("/save-employee-status")
	@Transactional
	public String getRecord(
			@Valid EmployeeStatus employeeStatus
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/employee-status/employee-status-list";
		}
		
		if(!Objects.isNull(employeeStatus.getId())){
			EmployeeStatus employeeStatusModel = employeeStatusRepository.findById(employeeStatus.getId()).get();
			employeeStatus.setActive(employeeStatusModel.isActive());
		}
        employeeStatus.setEmployeeStatusName(employeeStatus.getEmployeeStatusName().toUpperCase());
		employeeStatusRepository.save(employeeStatus);

		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/employee-status";
	}

	@PostMapping("/update-employee-status/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect) {
		EmployeeStatus employeeStatus = employeeStatusRepository.findById(id).get();

		employeeStatus.setActive(!employeeStatus.isActive());

		employeeStatusRepository.save(employeeStatus);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/employee-status";
	}

}
