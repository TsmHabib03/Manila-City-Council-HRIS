package com.ian.web.systemsettings.position_title;

import java.util.stream.Collectors;

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
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.appointment.AppointmentRepository;
import com.ian.web.employee.servicerecord.ServiceRecordRepository;
import com.ian.web.systemsettings.common.SettingsDeleteUtil;
import com.ian.web.systemsettings.department.DepartmentRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatus;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.levels.Level;
import com.ian.web.systemsettings.levels.LevelRepository;
import com.ian.web.systemsettings.salary_grades.SalaryGrade;
import com.ian.web.systemsettings.salary_grades.SalaryGradeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class PositionTitleController {

    private final PositionTitleRepository positionTitleRepository;
    private final EmployeeStatusRepository employeeStatusRepository;
    private final LevelRepository levelRepository;
    private final SalaryGradeRepository salaryGradeRepository;
    private final EmployeeRepository employeeRepository;
    private final ServiceRecordRepository serviceRecordRepository;
    private final AppointmentRepository appointmentRepository;
    private final DepartmentRepository departmentRepository;


    @GetMapping("/position-titles")
    public String getData(Model model) {
        Iterable<PositionTitle> listOfPositionTitle = positionTitleRepository.findAll();
        
        model.addAttribute("listOfPositionTitle",listOfPositionTitle);
        model.addAttribute("positionTitle", new PositionTitle());
        model.addAttribute("departmentList", departmentRepository.findAll());
        return "system-settings/position-title/position-title-list";
    }

    @PostMapping("/save-position-title")
	@Transactional
	public String getRecord(
			@Valid PositionTitle positionTitle
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {

		if (errors.hasErrors()) {
            Iterable<EmployeeStatus> listOfEmployeestatus = employeeStatusRepository.findAll().stream().filter((v)->v.isActive()).collect(Collectors.toList());
            Iterable<Level> listOfLevel = levelRepository.findAll().stream().filter((v)->v.isActive()).collect(Collectors.toList());
            Iterable<SalaryGrade> listOfSalaryGrade = salaryGradeRepository.findAll().stream().filter((v)->v.isActive()).collect(Collectors.toList());

            model.addAttribute("listOfEmployeeStatus",listOfEmployeestatus);
            model.addAttribute("listOfLevel",listOfLevel);
            model.addAttribute("listOfSalaryGrade",listOfSalaryGrade);
            model.addAttribute("departmentList", departmentRepository.findAll());

            model.addAttribute("positionTitle", positionTitle);
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/position-title/position-title-list";
		}
     
        
        positionTitle.setPositionTitleName(positionTitle.getPositionTitleName().toUpperCase());
//        positionTitle.setDepartmentCode(positionTitleModel.getDepartmentCode().toUpperCase());
//        positionTitle.setEmployeeStatus(employeeStatusRepository.save(employeeStatusRepository.findById(positionTitleModel.getEmployeeStatusId()).get()));
//        positionTitle.setLevel(levelRepository.save(levelRepository.save(levelRepository.findById(positionTitleModel.getLevelId()).get())));
//        positionTitle.setSalaryGrade(salaryGradeRepository.save(salaryGradeRepository.findById(positionTitleModel.getSalaryGradeId()).get()));
        positionTitleRepository.save(positionTitle);
        
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/position-titles";
	}

	@PostMapping("/delete-position-title/{id}")
	public String deletePositionTitle(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/position-titles";
		}
		if (employeeRepository.existsByPositionTitleId(id)
				|| serviceRecordRepository.existsByPositionTitleId(id)
				|| appointmentRepository.existsByPositionTitleId(id)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
				"Cannot delete this Position Title. It is still assigned to one or more employees, service records, or appointments."));
			return "redirect:/position-titles";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> positionTitleRepository.deleteById(id), "Position Title"));
		return "redirect:/position-titles";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}
}