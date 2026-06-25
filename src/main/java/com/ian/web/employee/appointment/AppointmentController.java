package com.ian.web.employee.appointment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.employee.attachment.DocumentAttachment;
import com.ian.web.employee.attachment.DocumentAttachmentRepository;
import com.ian.web.fileupload.FileDTO;
import com.ian.web.fileupload.StorageService;
import com.ian.web.systemsettings.division.DivisionRepository;
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AppointmentController {

	private static final Logger log = LoggerFactory.getLogger(AppointmentController.class);
	private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "jpg", "jpeg", "png", "docx", "xlsx");
	private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

	private final EmployeeRepository employeeRepository;
	private final AppointmentRepository appointmentRepository;
	private final PositionTitleRepository positionTitleRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final DivisionRepository divisionRepository;
	private final StorageService storageService;
	private final DocumentAttachmentRepository documentAttachmentRepository;

	@GetMapping("/appointments")
	public String viewAppointments(Model model) {
		model.addAttribute("employeeList", employeeRepository.findAll());
		return "employee/appointments/employee-list-appointments";
	}

	@GetMapping("/appointments/{employeeId}/{empHashCode}")
	public String viewEmployeeAppointments(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		populateAppointmentModel(model, employeeId, empHashCode, "HRADMIN");
		return "employee/appointments/employee-appointment-record";
	}

	@GetMapping("/my-appointments/{employeeId}/{empHashCode}")
	public String viewMyAppointments(Model model, @PathVariable long employeeId, @PathVariable String empHashCode) {
		populateAppointmentModel(model, employeeId, empHashCode, "EMPLOYEE");
		return "employee/appointments/employee-appointment-record";
	}

	@PostMapping({"/addAppointment", "/updateAppointment"})
	public String saveAppointment(
			Appointment appointment,
			final RedirectAttributes redirect,
			HttpServletRequest request,
			@RequestParam(value = "attachedFiles", required = false) MultipartFile[] attachedFiles) {

		Appointment saved = appointmentRepository.save(appointment);

		saveAttachments(attachedFiles, "APPOINTMENT", saved.getId(), saved.getEmployee(), request);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Saved."));
		return "redirect:/appointments/" + saved.getEmployee().getId()
				+ "/" + saved.getEmployee().getEmpHashCode();
	}

	@GetMapping("/deleteAppointment/{id}")
	@Transactional
	public String deleteAppointment(@PathVariable long id, final RedirectAttributes redirect) {
		Appointment appt = appointmentRepository.findById(id).orElseThrow();
		long employeeId = appt.getEmployee().getId();
		String hashCode = appt.getEmployee().getEmpHashCode();
		documentAttachmentRepository.deleteAllByModuleAndRecordId("APPOINTMENT", id);
		appointmentRepository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Deleted."));
		return "redirect:/appointments/" + employeeId + "/" + hashCode;
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private void populateAppointmentModel(Model model, long employeeId, String empHashCode, String showMode) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		if (!optional.isPresent()) {
			model.addAttribute("msg", new UXMessage("EMP-NOT-FOUND",
					"Employee Not Found. You will be redirected to the dashboard."));
		}

		Employee employee = optional.orElseGet(Employee::new);
		model.addAttribute("employee", employee);

		Appointment appointment = new Appointment();
		appointment.setEmployee(employee);
		model.addAttribute("appointment", appointment);
		model.addAttribute("showMode", showMode);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAllByOrderByPositionTitleNameAsc());

		List<Appointment> appointmentList = appointmentRepository.findByEmployeeId(employeeId);
		model.addAttribute("appointmentRecordList", appointmentList);
		model.addAttribute("attachmentListMap", buildAttachmentMap(appointmentList));
	}

	private Map<Long, List<DocumentAttachment>> buildAttachmentMap(List<Appointment> list) {
		Map<Long, List<DocumentAttachment>> map = new HashMap<>();
		for (Appointment appt : list) {
			map.put(appt.getId(),
					documentAttachmentRepository.findByModuleAndRecordId("APPOINTMENT", appt.getId()));
		}
		return map;
	}

	private void saveAttachments(MultipartFile[] files, String module, long recordId,
			Employee employee, HttpServletRequest request) {
		if (files == null) return;
		String uploader = resolveUploader(request);
		for (MultipartFile file : files) {
			String origName = file.getOriginalFilename();
			if (origName == null || origName.isEmpty() || file.getSize() == 0) continue;
			int dot = origName.lastIndexOf('.');
			if (dot < 0) continue;
			String ext = origName.substring(dot + 1).toLowerCase();
			if (!ALLOWED_TYPES.contains(ext) || file.getSize() > MAX_SIZE_BYTES) continue;
			try {
				String storedName = "emp_appt_" + employee.getId() + "_" + System.currentTimeMillis() + "." + ext;
				FileDTO dto = storageService.uploadFile(file, storedName);
				DocumentAttachment att = new DocumentAttachment();
				att.setOriginalFileName(origName);
				att.setFileUrl(dto.getDownloadUri());
				att.setFileType(ext);
				att.setFileSizeBytes(file.getSize());
				att.setUploadedAt(LocalDateTime.now());
				att.setUploadedBy(uploader);
				att.setModule(module);
				att.setRecordId(recordId);
				att.setEmployee(employee);
				documentAttachmentRepository.save(att);
			} catch (Exception e) {
				log.error("Failed to upload attachment for module={} recordId={}", module, recordId, e);
			}
		}
	}

	private String resolveUploader(HttpServletRequest request) {
		Object actor = request.getSession(false) != null
				? request.getSession(false).getAttribute("actorObj") : null;
		if (actor instanceof Employee) return ((Employee) actor).getUsername();
		return "admin";
	}
}
