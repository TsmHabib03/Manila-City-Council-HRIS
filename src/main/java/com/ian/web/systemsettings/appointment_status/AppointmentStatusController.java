package com.ian.web.systemsettings.appointment_status;

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
public class AppointmentStatusController {

    private final AppointmentStatusRepository appointmentStatusRepository;

    @GetMapping("/appointment-statuses")
    public String getData(Model model) {
        Iterable<AppointmentStatus> listOfAppointmentStatus = appointmentStatusRepository.findAll();
        model.addAttribute("listOfAppointmentStatus",listOfAppointmentStatus);
        model.addAttribute("appointmentStatus", new AppointmentStatus());
        return "system-settings/appointment-status/appointment-status-list";
    }

    @PostMapping("/save-appointment-status")
	@Transactional
	public String getRecord(
			@Valid AppointmentStatus appointmentStatus
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/appointment-status/appointment-status-list";
		}

		if(!Objects.isNull(appointmentStatus.getId())){
			AppointmentStatus appointmentStatusModel = appointmentStatusRepository.findById(appointmentStatus.getId()).get();
			appointmentStatus.setActive(appointmentStatusModel.isActive());
		}
        appointmentStatus.setAppointmentStatusName(appointmentStatus.getAppointmentStatusName().toUpperCase());
		appointmentStatusRepository.save(appointmentStatus);

		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/appointment-statuses";
	}

	@PostMapping("/delete-appointment-status/{id}")
	public String deleteAppointmentStatus(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/appointment-statuses";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> appointmentStatusRepository.deleteById(id), "Appointment Status"));
		return "redirect:/appointment-statuses";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}
}
