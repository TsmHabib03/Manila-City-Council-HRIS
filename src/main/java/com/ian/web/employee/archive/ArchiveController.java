package com.ian.web.employee.archive;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.fileupload.FileDTO;
import com.ian.web.fileupload.StorageService;

import lombok.RequiredArgsConstructor;

/**
 * Archive module (CR Request ID 015): per-section file repositories for
 * SALN, Resigned Employees and past Leaves, with customizable labels, tags,
 * search and client-side CSV/XLS export. Routes are staff-only via
 * SecurityConfig.ARCHIVE_ADMIN.
 */
@Controller
@RequiredArgsConstructor
public class ArchiveController {

	static final Map<String, String> SECTIONS = Map.of(
			"SALN", "SALN",
			"RESIGNED", "Resigned Employees",
			"LEAVES", "Leaves");

	private final ArchiveFileRepository archiveFileRepository;
	private final ArchiveLabelRepository archiveLabelRepository;
	private final StorageService storageService;

	@GetMapping("/archive/{section}")
	public String viewArchive(Model model, @PathVariable String section) {
		if (!SECTIONS.containsKey(section)) {
			return "redirect:/dashboard";
		}
		model.addAttribute("section", section);
		model.addAttribute("sectionTitle", SECTIONS.get(section));
		model.addAttribute("fileList", archiveFileRepository.findBySectionOrderByIdDesc(section));
		model.addAttribute("labelList", archiveLabelRepository.findBySectionOrderByLabelNameAsc(section));
		return "employee/archive/archive-list";
	}

	@PostMapping("/archive/upload")
	public String uploadArchiveFile(
			@RequestParam("section") String section,
			@RequestParam("docFile") MultipartFile docFile,
			@RequestParam(value = "label", required = false) String label,
			@RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "employeeName", required = false) String employeeName,
			@RequestParam(value = "department", required = false) String department,
			@RequestParam(value = "docType", required = false) String docType,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "refDate", required = false)
				@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate refDate,
			HttpServletRequest request,
			final RedirectAttributes redirect) throws Exception {

		if (!SECTIONS.containsKey(section) || docFile == null || docFile.isEmpty()) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Please choose a file to upload."));
			return "redirect:/archive/" + (SECTIONS.containsKey(section) ? section : "SALN");
		}

		String original = docFile.getOriginalFilename() == null ? "file" : docFile.getOriginalFilename();
		String ext = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
		String storedName = "archive_" + section.toLowerCase() + "_" + System.currentTimeMillis() + ext;
		FileDTO fileDTO = storageService.uploadFile(docFile, storedName);

		ArchiveFile file = new ArchiveFile();
		file.setSection(section);
		file.setFileName(original);
		file.setFileUrl(fileDTO.getDownloadUri());
		file.setFileType(ext.isEmpty() ? "" : ext.substring(1).toUpperCase());
		Object actorObj = request.getSession().getAttribute("actorObj");
		file.setUploader(actorObj instanceof Employee
				? ((Employee) actorObj).getFirstName() + " " + ((Employee) actorObj).getLastName() : "");
		file.setUploadDate(LocalDate.now());
		file.setRefDate(refDate);
		file.setEmployeeName(employeeName);
		file.setDepartment(department);
		file.setDocType(docType);
		file.setStatus(status);
		file.setLabel(label);
		file.setTags(tags);
		archiveFileRepository.save(file);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "File uploaded."));
		return "redirect:/archive/" + section;
	}

	/** Update the metadata (not the stored file) of an archived document. */
	@PostMapping("/archive/update")
	public String updateArchiveFile(
			@RequestParam("id") long id,
			@RequestParam(value = "label", required = false) String label,
			@RequestParam(value = "tags", required = false) String tags,
			@RequestParam(value = "employeeName", required = false) String employeeName,
			@RequestParam(value = "department", required = false) String department,
			@RequestParam(value = "docType", required = false) String docType,
			@RequestParam(value = "status", required = false) String status,
			@RequestParam(value = "refDate", required = false)
				@org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate refDate,
			final RedirectAttributes redirect) {

		ArchiveFile file = archiveFileRepository.findById(id).orElseThrow();
		file.setLabel(label);
		file.setTags(tags);
		file.setEmployeeName(employeeName);
		file.setDepartment(department);
		file.setDocType(docType);
		file.setStatus(status);
		file.setRefDate(refDate);
		archiveFileRepository.save(file);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Saved."));
		return "redirect:/archive/" + file.getSection();
	}

	@GetMapping("/archive/delete/{id}")
	public String deleteArchiveFile(@PathVariable long id, final RedirectAttributes redirect) {
		ArchiveFile file = archiveFileRepository.findById(id).orElseThrow();
		String section = file.getSection();
		archiveFileRepository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Deleted."));
		return "redirect:/archive/" + section;
	}

	/** Adds a new label or renames an existing one (id > 0). */
	@PostMapping("/archive/saveLabel")
	public String saveLabel(
			@RequestParam("section") String section,
			@RequestParam(value = "id", required = false, defaultValue = "0") long id,
			@RequestParam("labelName") String labelName,
			final RedirectAttributes redirect) {

		if (!SECTIONS.containsKey(section) || labelName == null || labelName.isBlank()) {
			redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Label name is required."));
			return "redirect:/archive/" + (SECTIONS.containsKey(section) ? section : "SALN");
		}
		ArchiveLabel labelEntity = id > 0
				? archiveLabelRepository.findById(id).orElseGet(ArchiveLabel::new)
				: new ArchiveLabel();
		String oldName = labelEntity.getLabelName();
		labelEntity.setSection(section);
		labelEntity.setLabelName(labelName.trim());
		archiveLabelRepository.save(labelEntity);

		// Renaming a label re-labels the files already grouped under it
		if (id > 0 && oldName != null && !oldName.equals(labelName.trim())) {
			List<ArchiveFile> files = archiveFileRepository.findBySectionOrderByIdDesc(section);
			for (ArchiveFile f : files) {
				if (oldName.equals(f.getLabel())) {
					f.setLabel(labelName.trim());
					archiveFileRepository.save(f);
				}
			}
		}

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Label saved."));
		return "redirect:/archive/" + section;
	}

	@GetMapping("/archive/deleteLabel/{id}")
	public String deleteLabel(@PathVariable long id, final RedirectAttributes redirect) {
		ArchiveLabel label = archiveLabelRepository.findById(id).orElseThrow();
		String section = label.getSection();
		archiveLabelRepository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Label deleted."));
		return "redirect:/archive/" + section;
	}
}
