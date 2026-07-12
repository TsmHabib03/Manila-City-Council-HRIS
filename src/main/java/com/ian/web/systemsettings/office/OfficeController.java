package com.ian.web.systemsettings.office;

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
public class OfficeController {

    private final OfficeRepository officeRepository;

    @GetMapping("/offices")
    public String getData(Model model) {
        Iterable<Office> listOfOffice = officeRepository.findAll();
        model.addAttribute("listOfOffice",listOfOffice);
        model.addAttribute("office", new Office());
        return "system-settings/office/office-list";
    }

    @PostMapping("/save-office")
	@Transactional
	public String getRecord(
			@Valid Office office
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/office/office-list";
		}

		if(!Objects.isNull(office.getId())){
			Office officeModel = officeRepository.findById(office.getId()).get();
			office.setActive(officeModel.isActive());
		}
        office.setOfficeName(office.getOfficeName().toUpperCase());
		officeRepository.save(office);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/offices";
	}

	@PostMapping("/delete-office/{id}")
	public String deleteOffice(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/offices";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> officeRepository.deleteById(id), "Office"));
		return "redirect:/offices";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}
}
