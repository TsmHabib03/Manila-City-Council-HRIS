package com.ian.web.systemsettings.position_title;

import java.util.stream.Collectors;

import javax.transaction.Transactional;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
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


    @GetMapping("/position-titles")
    public String getData(Model model) {
        Iterable<PositionTitle> listOfPositionTitle = positionTitleRepository.findAll();
        
        model.addAttribute("listOfPositionTitle",listOfPositionTitle);
        model.addAttribute("positionTitle", new PositionTitle());
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
}