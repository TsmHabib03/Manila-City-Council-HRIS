package com.ian.web.systemsettings.document_type;

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
import com.ian.web.employee.docs201.Docs201Repository;
import com.ian.web.systemsettings.common.SettingsDeleteUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DocumentTypeController {

    private final DocumentTypeRepository documentTypeRepository;
    private final Docs201Repository docs201Repository;

    @GetMapping("/document-types")
	public String listAll(Model model) {
		Iterable<DocumentType> documentTypeList = documentTypeRepository.findAll();
		model.addAttribute("documentTypeList", documentTypeList);
		model.addAttribute("documentType", new DocumentType());
		return "system-settings/document-type/document-type-list";
	}
	
	@PostMapping("/save-document-type")
	@Transactional
	public String saveAcademicHonors(
			@Valid DocumentType documentType
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			) {
		if (errors.hasErrors()) {
			model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
			return "system-settings/document-type/document-type-list";
		}
		if(!Objects.isNull(documentType.getId())){
			DocumentType documentTypeModel = documentTypeRepository.findById(documentType.getId()).get();
			documentType.setActive(documentTypeModel.isActive());
		}		
		documentType.setDocumentName(documentType.getDocumentName().toUpperCase());
		documentTypeRepository.save(documentType);		
		
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
		return "redirect:/document-types";
	}

	@PostMapping("/update-document-type-status/{id}")
	public String updateStatus(@PathVariable("id") Long id, final RedirectAttributes redirect) {
		DocumentType documentType = documentTypeRepository.findById(id).get();
		documentType.setActive(!documentType.isActive());
		documentTypeRepository.save(documentType);
		redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully update."));
		return "redirect:/document-types";
	}

	@PostMapping("/delete-document-type/{id}")
	public String deleteDocumentType(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
		if (!isAdmin(request)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
			return "redirect:/document-types";
		}
		if (docs201Repository.existsByDocumentTypeId(id)) {
			redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR",
				"Cannot delete this Document Type. It is still assigned to one or more 201 documents."));
			return "redirect:/document-types";
		}
		redirect.addFlashAttribute("uxmessage",
			SettingsDeleteUtil.tryDelete(() -> documentTypeRepository.deleteById(id), "Document Type"));
		return "redirect:/document-types";
	}

	private boolean isAdmin(HttpServletRequest request) {
		Object actorObj = request.getSession().getAttribute("actorObj");
		return actorObj instanceof com.ian.web.employee.Employee
			&& "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
	}

}
