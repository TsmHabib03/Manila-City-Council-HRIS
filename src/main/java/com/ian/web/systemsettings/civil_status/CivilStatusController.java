package com.ian.web.systemsettings.civil_status;

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
public class CivilStatusController {

    private final CivilStatusRepository civilStatusRepository;

    @GetMapping("/civil-statuses")
    public String getData(Model model) {
        Iterable<CivilStatus> listOfCivilStatus = civilStatusRepository.findAll();
        model.addAttribute("listOfCivilStatus",listOfCivilStatus);
        model.addAttribute("civilStatus", new CivilStatus());
        return "system-settings/civil-status/civil-status-list";
    }

    @PostMapping("/save-civil-status")
	@Transactional
	public String getRecord(
			@Valid CivilStatus civilStatus
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/civil-status/civil-status-list";
		}

		if(!Objects.isNull(civilStatus.getId())){
			CivilStatus civilStatusModel = civilStatusRepository.findById(civilStatus.getId()).get();
			civilStatus.setActive(civilStatusModel.isActive());
		}
        civilStatus.setCivilStatusName(civilStatus.getCivilStatusName().toUpperCase());
		civilStatusRepository.save(civilStatus);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/civil-statuses";
	}

	@PostMapping("/delete-civil-status/{id}")
	public String deleteCivilStatus(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/civil-statuses";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> civilStatusRepository.deleteById(id), "Civil Status"));
		return "redirect:/civil-statuses";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}
}
