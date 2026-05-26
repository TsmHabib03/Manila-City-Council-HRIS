package com.ian.web.systemsettings.document_type;

import java.util.Objects;

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

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class DocumentTypeController {
    
    private final DocumentTypeRepository documentTypeRepository;

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

}
