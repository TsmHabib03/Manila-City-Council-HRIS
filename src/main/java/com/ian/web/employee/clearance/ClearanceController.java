package com.ian.web.employee.clearance;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ClearanceController {
	
	private final EmployeeRepository employeeRepository;
	private final ClearanceRepository clearanceRepository;
	
	@GetMapping("/clearance-list")
	public String getAllClearance(Model model) {
		return "employee/clearance/clearance-list";
	}
	
	@GetMapping("/myclearance/{employeeId}/{empHashCode}")
	public String viewEmployeeClearance(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		
		model.addAttribute("employee", employee);
		
		Clearance clearance = new Clearance();
		clearance.setEmployee(employee);
		clearance.setId(0L);
		
		model.addAttribute("clearance", clearance);
		
		List<Clearance> clearanceList = clearanceRepository.findByEmployeeId(employeeId);
		model.addAttribute("clearanceList", clearanceList);
		
		if (request.getServletPath().startsWith("/profile")) {
			model.addAttribute("showMode", "PROFILE");
		} else {
			model.addAttribute("showMode", "HRADMIN");
		}		
		
		
		return "employee/clearance/my-clearance";
		
	}
	
	@PostMapping({"/addMyClearance", "/addClearance"})
	public String saveClearance(
			@Valid Clearance clearance
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
	    	    
		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			Long empId = (clearance.getEmployee() != null && clearance.getEmployee().getId() > 0)
					? clearance.getEmployee().getId() : null;
			model.addAttribute("clearanceList", empId != null ? clearanceRepository.findByEmployeeId(empId) : List.of());
			return "employee/clearance/clearance-list";
		}

		if (clearance.getEmployee() == null || clearance.getEmployee().getId() == 0) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Invalid employee record. Please try again."));
			return "redirect:/dashboard";
		}

		if(clearance.getId() == 0) {
			clearance.setTransDate(LocalDate.now());
			clearance.setStatus("SUBMITTED");
		} else {
			Optional<Clearance> optional =  clearanceRepository.findById(clearance.getId());
			Clearance oldRecord = optional.orElseGet(() -> new Clearance());

			clearance.setTransDate(oldRecord.getTransDate());
			clearance.setStatus(oldRecord.getStatus());
		}

		clearance = clearanceRepository.save(clearance);

		if (request.getServletPath().equalsIgnoreCase("/addMyClearance")) {
			Employee emp = employeeRepository.findById(clearance.getEmployee().getId()).orElseThrow();
			redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Saved."));
			return "redirect:/myclearance/" + emp.getId() + "/" + emp.getEmpHashCode();
		} else {
			return "redirect:/clearance-list";
		}
	}
	
	

}
