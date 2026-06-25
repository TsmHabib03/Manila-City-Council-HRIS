package com.ian.web.employee.clearance;

import java.time.LocalDate;
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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class ClearanceController {

	private static final Logger log = LoggerFactory.getLogger(ClearanceController.class);
	private static final Set<String> ALLOWED_TYPES = Set.of("pdf", "jpg", "jpeg", "png", "docx", "xlsx");
	private static final long MAX_SIZE_BYTES = 10L * 1024 * 1024;

	private final EmployeeRepository employeeRepository;
	private final ClearanceRepository clearanceRepository;
	private final StorageService storageService;
	private final DocumentAttachmentRepository documentAttachmentRepository;

	@GetMapping("/clearance-list")
	public String getAllClearance(Model model) {
		return "employee/clearance/clearance-list";
	}

	@GetMapping("/myclearance/{employeeId}/{empHashCode}")
	public String viewEmployeeClearance(Model model, @PathVariable long employeeId,
			@PathVariable String empHashCode, HttpServletRequest request) {

		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		if (!optional.isPresent()) {
			model.addAttribute("msg", new UXMessage("EMP-NOT-FOUND",
					"Employee Not Found. You will be redirected to the dashboard."));
		}

		Employee employee = optional.orElseGet(Employee::new);
		model.addAttribute("employee", employee);

		Clearance clearance = new Clearance();
		clearance.setEmployee(employee);
		clearance.setId(0L);
		model.addAttribute("clearance", clearance);

		List<Clearance> clearanceList = clearanceRepository.findByEmployeeId(employeeId);
		model.addAttribute("clearanceList", clearanceList);
		model.addAttribute("attachmentListMap", buildAttachmentMap(clearanceList));

		model.addAttribute("showMode",
				request.getServletPath().startsWith("/profile") ? "PROFILE" : "HRADMIN");

		return "employee/clearance/my-clearance";
	}

	@PostMapping({"/addMyClearance", "/addClearance"})
	public String saveClearance(
			@Valid Clearance clearance,
			Errors errors,
			final RedirectAttributes redirect,
			Model model,
			HttpServletRequest request,
			@RequestParam(value = "attachedFiles", required = false) MultipartFile[] attachedFiles) {

		if (errors.hasErrors()) {
			model.addAttribute("msg", new UXMessage("ERROR", "Please check items marked in red."));
			Long empId = (clearance.getEmployee() != null && clearance.getEmployee().getId() > 0)
					? clearance.getEmployee().getId() : null;
			List<Clearance> list = empId != null
					? clearanceRepository.findByEmployeeId(empId) : List.of();
			model.addAttribute("clearanceList", list);
			model.addAttribute("attachmentListMap", buildAttachmentMap(list));
			return "employee/clearance/my-clearance";
		}

		if (clearance.getEmployee() == null || clearance.getEmployee().getId() == 0) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Invalid employee record. Please try again."));
			return "redirect:/dashboard";
		}

		if (clearance.getId() == null || clearance.getId() == 0) {
			clearance.setTransDate(LocalDate.now());
			clearance.setStatus("SUBMITTED");
		} else {
			Optional<Clearance> oldOpt = clearanceRepository.findById(clearance.getId());
			if (oldOpt.isPresent()) {
				clearance.setTransDate(oldOpt.get().getTransDate());
				clearance.setStatus(oldOpt.get().getStatus());
			}
		}

		Clearance saved = clearanceRepository.save(clearance);

		saveAttachments(attachedFiles, "CLEARANCE", saved.getId(), saved.getEmployee(), request);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Saved."));

		if (request.getServletPath().equalsIgnoreCase("/addMyClearance")) {
			Employee emp = employeeRepository.findById(saved.getEmployee().getId()).orElseThrow();
			return "redirect:/myclearance/" + emp.getId() + "/" + emp.getEmpHashCode();
		}
		return "redirect:/clearance-list";
	}

	// ── helpers ──────────────────────────────────────────────────────────────

	private Map<Long, List<DocumentAttachment>> buildAttachmentMap(List<Clearance> list) {
		Map<Long, List<DocumentAttachment>> map = new HashMap<>();
		for (Clearance c : list) {
			if (c.getId() != null && c.getId() > 0) {
				map.put(c.getId(),
						documentAttachmentRepository.findByModuleAndRecordId("CLEARANCE", c.getId()));
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
				String storedName = "emp_clr_" + employee.getId() + "_" + System.currentTimeMillis() + "." + ext;
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
