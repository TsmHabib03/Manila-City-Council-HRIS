package com.ian.web.systemsettings.department;

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
import com.ian.web.systemsettings.common.SettingsDeleteUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DepartmentController {

    private final DepartmentRepository departmentRepository;

    @GetMapping("/departments")
    public String getData(Model model) {
        Iterable<Department> listOfDepartment = departmentRepository.findAll();
        model.addAttribute("listOfDepartment",listOfDepartment);
        model.addAttribute("department", new Department());
        return "system-settings/department/department-list";
    }

    @PostMapping("/save-department")
	@Transactional
	public String getRecord(
			@Valid Department department
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/department/department-list";
		}

		if(!Objects.isNull(department.getId())){
			Department departmentModel = departmentRepository.findById(department.getId()).get();
			department.setActive(departmentModel.isActive());
		}
        department.setDepartmentName(department.getDepartmentName().toUpperCase());
		departmentRepository.save(department);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/departments";
	}

	@PostMapping("/delete-department/{id}")
	public String deleteDepartment(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/departments";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> departmentRepository.deleteById(id), "Department"));
		return "redirect:/departments";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}
}
