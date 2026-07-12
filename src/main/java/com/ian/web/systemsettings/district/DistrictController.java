package com.ian.web.systemsettings.district;

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
import com.ian.web.systemsettings.common.SettingsDeleteUtil;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DistrictController {

	private final DistrictRepository districtRepository;
	private final EmployeeRepository employeeRepository;

	@GetMapping("/districts")
    public String getAllDistricts(Model model) {
        Iterable<District> districtList = districtRepository.findAll();
        model.addAttribute("districtList", districtList);
		model.addAttribute("district", new District());
        return "system-settings/district/district-list";
    }
	
	@PostMapping("/save-district")
	@Transactional
	public String saveRecord(
			@Valid District district
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/district/district-list";
		}
		
		district.setDistrictName(district.getDistrictName().toUpperCase());
		districtRepository.save(district);		
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/districts";
	}

	@PostMapping("/delete-district/{id}")
	public String deleteDistrict(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/districts";
		}
		if (employeeRepository.existsByDistrictId(id)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
				"Cannot delete this District. It is still assigned to one or more employees."));
			return "redirect:/districts";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> districtRepository.deleteById(id), "District"));
		return "redirect:/districts";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}

}
