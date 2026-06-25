package com.ian.web.employee.servicerecord;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
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
import com.ian.web.systemsettings.employee_status.EmployeeStatusRepository;
import com.ian.web.systemsettings.position_title.PositionTitleRepository;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ServiceRecordController {

	private static final Logger log = LoggerFactory.getLogger(ServiceRecordController.class);
	private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "jpg", "jpeg", "png", "docx", "xlsx");
	private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

	private final EmployeeRepository employeeRepository;
	private final ServiceRecordRepository serviceRecordRepository;
	private final EmployeeStatusRepository employeeStatusRepository;
	private final PositionTitleRepository positionTitleRepository;
	private final StorageService storageService;
	private final DocumentAttachmentRepository documentAttachmentRepository;

	@GetMapping("/employee-service-record/{employeeId}/{empHashCode}")
	@Transactional
	public String viewEmployeeServiceRecord(Model model, @PathVariable long employeeId,
			@PathVariable String empHashCode, HttpServletRequest request) {

		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		if (!optional.isPresent()) {
			UXMessage msg = new UXMessage("EMP-NOT-FOUND", "Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}

		Employee employee = optional.orElseGet(Employee::new);
		model.addAttribute("employee", employee);

		ServiceRecord serviceRecord = new ServiceRecord();
		serviceRecord.setEmployee(employee);
		model.addAttribute("serviceRecord", serviceRecord);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());
		model.addAttribute("positionTitleList", positionTitleRepository.findAllByOrderByPositionTitleNameAsc());

		List<ServiceRecord> serviceRecordList = serviceRecordRepository.findByEmployeeIdOrderByDateFromDesc(employeeId);
		adjustDates(serviceRecordList);
		model.addAttribute("serviceRecordList", serviceRecordList);

		model.addAttribute("attachmentListMap", buildAttachmentMap(serviceRecordList));

		model.addAttribute("showMode", request.getServletPath().startsWith("/profile") ? "PROFILE" : "HRADMIN");

		return "employee/service-record/employee-service-record";
	}

	@GetMapping("/my-service-record/{employeeId}/{empHashCode}")
	public String viewMyServiceRecord(Model model, @PathVariable long employeeId,
			@PathVariable String empHashCode, HttpServletRequest request) {

		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		if (!optional.isPresent()) {
			UXMessage msg = new UXMessage("EMP-NOT-FOUND", "Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}

		Employee employee = optional.orElseGet(Employee::new);
		model.addAttribute("employee", employee);

		ServiceRecord serviceRecord = new ServiceRecord();
		serviceRecord.setEmployee(employee);
		model.addAttribute("serviceRecord", serviceRecord);
		model.addAttribute("employeeStatusList", employeeStatusRepository.findAll());

		List<ServiceRecord> serviceRecordList = serviceRecordRepository.findByEmployeeId(employeeId);
		model.addAttribute("serviceRecordList", serviceRecordList);
		model.addAttribute("attachmentListMap", buildAttachmentMap(serviceRecordList));

		model.addAttribute("showMode", request.getServletPath().startsWith("/profile") ? "PROFILE" : "HRADMIN");

		return "employee/service-record/my-service-record";
	}

	@PostMapping({"/addMyServiceRecord", "/addServiceRecord"})
	public String saveServiceRecord(
			@Valid ServiceRecord serviceRecord,
			Errors errors,
			final RedirectAttributes redirect,
			Model model,
			HttpServletRequest request,
			@RequestParam(value = "attachedFiles", required = false) MultipartFile[] attachedFiles) {

		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			List<ServiceRecord> list = serviceRecordRepository.findByEmployeeIdOrderByDateFromDesc(serviceRecord.getEmployee().getId());
			model.addAttribute("serviceRecordList", list);
			model.addAttribute("attachmentListMap", buildAttachmentMap(list));
			if (request.getServletPath().equalsIgnoreCase("/addMyServiceRecord")) {
				return "employee/service-record/my-service-record";
			}
			return "employee/service-record/employee-service-record";
		}

		serviceRecord = serviceRecordRepository.save(serviceRecord);

		saveAttachments(attachedFiles, "SERVICE_RECORD", serviceRecord.getId(),
				serviceRecord.getEmployee(), request);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));

		if (request.getServletPath().equalsIgnoreCase("/addMyServiceRecord")) {
			return "redirect:/my-service-record/" + serviceRecord.getEmployee().getId()
					+ "/" + serviceRecord.getEmployee().getEmpHashCode();
		}
		return "redirect:/employee-service-record/" + serviceRecord.getEmployee().getId()
				+ "/" + serviceRecord.getEmployee().getEmpHashCode();
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private Map<Long, List<DocumentAttachment>> buildAttachmentMap(List<ServiceRecord> list) {
		Map<Long, List<DocumentAttachment>> map = new HashMap<>();
		for (ServiceRecord sr : list) {
			if (sr.getId() != null) {
				map.put(sr.getId(),
						documentAttachmentRepository.findByModuleAndRecordId("SERVICE_RECORD", sr.getId()));
			}
		}
		return map;
	}

	private void saveAttachments(MultipartFile[] files, String module, Long recordId,
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
				String storedName = "emp_sr_" + employee.getId() + "_" + System.currentTimeMillis() + "." + ext;
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

	private void adjustDates(List<ServiceRecord> list) {
		list.forEach(sr -> {
			if (sr.getDateFrom() != null)          sr.setDateFrom(sr.getDateFrom().plusDays(1));
			if (sr.getDateTo() != null)             sr.setDateTo(sr.getDateTo().plusDays(1));
			if (sr.getEntranceDate() != null)       sr.setEntranceDate(sr.getEntranceDate().plusDays(1));
			if (sr.getSeparationDate() != null)     sr.setSeparationDate(sr.getSeparationDate().plusDays(1));
			if (sr.getSigningDate() != null)        sr.setSigningDate(sr.getSigningDate().plusDays(1));
			if (sr.getPublicationDateFrom() != null) sr.setPublicationDateFrom(sr.getPublicationDateFrom().plusDays(1));
			if (sr.getPublicationDateTo() != null)  sr.setPublicationDateTo(sr.getPublicationDateTo().plusDays(1));
			if (sr.getDateOfActionCscAction() != null)  sr.setDateOfActionCscAction(sr.getDateOfActionCscAction().plusDays(1));
			if (sr.getDateOfReleaseCscAction() != null) sr.setDateOfReleaseCscAction(sr.getDateOfReleaseCscAction().plusDays(1));
		});
	}
}
