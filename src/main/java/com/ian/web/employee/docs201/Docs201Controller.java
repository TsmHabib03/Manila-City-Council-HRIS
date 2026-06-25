package com.ian.web.employee.docs201;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import com.ian.web.fileupload.FileDTO;
import com.ian.web.fileupload.StorageService;
import com.ian.web.systemsettings.document_type.DocumentType;
import com.ian.web.systemsettings.document_type.DocumentTypeRepository;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
@RequiredArgsConstructor
public class Docs201Controller {

	private static final Logger log = LoggerFactory.getLogger(Docs201Controller.class);
	
	private final EmployeeRepository employeeRepository;
	private final Docs201Repository docs201Repository;
	private final DocumentTypeRepository documentTypeRepository;
	private final StorageService storageService;
	
	@GetMapping("/201files")
	public String view201FilesList(Model model) {
		List<Employee> employeeList = employeeRepository.findAll();
		model.addAttribute("employeeList", employeeList);
		return "employee/docs201/employee-list-201files";
	}

	@GetMapping("/201files/{employeeId}/{empHashCode}")
	public String viewEmployeeClearance(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		
		model.addAttribute("employee", employee);
		
		Docs201 docs201 = new Docs201();
		docs201.setEmployee(employee);
		docs201.setId(0L);
		
		model.addAttribute("docs201", docs201);
		
		List<Docs201> docsList = docs201Repository.findByEmployeeId(employeeId);
		model.addAttribute("docsList", docsList);
		
		List<DocumentType> documentTypeList = documentTypeRepository.findAll();
		model.addAttribute("documentTypeList", documentTypeList);
		
		model.addAttribute("showMode", "HRADMIN");		
		
		return "employee/docs201/docs201";
		
	}
	
	@GetMapping("/my201files/{employeeId}/{empHashCode}")
	public String viewMyEmployeeClearance(Model model, @PathVariable long employeeId, @PathVariable String empHashCode, HttpServletRequest request) {
		Optional<Employee> optional = employeeRepository.findByIdAndEmpHashCode(employeeId, empHashCode);
		UXMessage msg = new UXMessage();
		if(optional.isPresent()) {			
			
		} else {
			msg.setCode("EMP-NOT-FOUND");
			msg.setMessage("Employee Not Found. You will be redirected to the dashboard.");
			model.addAttribute("msg", msg);
		}
		
		Employee employee = optional.orElseGet(() -> new Employee());
		
		model.addAttribute("employee", employee);
		
		Docs201 docs201 = new Docs201();
		docs201.setEmployee(employee);
		docs201.setId(0L);
		
		model.addAttribute("docs201", docs201);
		
		List<Docs201> docsList = docs201Repository.findByEmployeeId(employeeId);
		model.addAttribute("docsList", docsList);
		
		List<DocumentType> documentTypeList = documentTypeRepository.findAll();
		model.addAttribute("documentTypeList", documentTypeList);
		
		model.addAttribute("showMode", "PROFILE");				
		
		return "employee/docs201/docs201";
		
	}
	
	@GetMapping("/delete201File/{id}")
	@Transactional
	public String delete201File(@PathVariable Long id, final RedirectAttributes redirect) {
		Docs201 doc = docs201Repository.findById(id).orElseThrow();
		long employeeId = doc.getEmployee().getId();
		String hashCode = doc.getEmployee().getEmpHashCode();
		docs201Repository.deleteById(id);
		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Deleted."));
		return "redirect:/201files/" + employeeId + "/" + hashCode;
	}

	@PostMapping({"/addMy201Files", "/add201Files"})
	@Transactional
	public String save201Files(
			@Valid Docs201 docs201
			,Errors errors
			,final RedirectAttributes redirect
			,Model model
			,HttpServletRequest request
			) {
		
        long empId = docs201.getEmployee().getId();
        if (errors.hasErrors()  || docs201.getDocumentType().getId() <= 0) {
        	if (docs201.getDocumentType().getId() <= 0) {
                errors.rejectValue("documentType", "documentType.id.zero", " is mandatory");
            }
            model.addAttribute("isValidationErrorOnAdd", true);
        	
            List<Docs201> docsList = this.docs201Repository.findByEmployeeId(empId);
            List<DocumentType> documentTypeList = this.documentTypeRepository.findAll();
            Optional<Employee> optionalEmployee = this.employeeRepository.findById(empId);
            Employee employee = optionalEmployee.get();
            model.addAttribute("employee", employee);
            model.addAttribute("docsList", docsList);
            model.addAttribute("documentTypeList", documentTypeList);
            model.addAttribute("docs201", docs201);
                
            return "employee/docs201/docs201";
        }		
		
		Employee employee = employeeRepository.findById(empId).orElseGet(() -> new Employee());
		DocumentType docType = documentTypeRepository.findById(docs201.getDocumentType().getId())
				    .orElseGet(() -> new DocumentType());

		// Use a managed entity so @ElementCollection updates correctly.
		// Detached-entity merge does not reliably update element collections.
		Docs201 entityToSave;
		if (docs201.getId() != null && docs201.getId() > 0) {
			entityToSave = docs201Repository.findById(docs201.getId()).orElseGet(Docs201::new);
		} else {
			entityToSave = new Docs201();
		}
		entityToSave.setTransDate(docs201.getTransDate());
		entityToSave.setRemarks(docs201.getRemarks());
		entityToSave.setEmployee(employee);
		entityToSave.setDocumentType(docType);

		MultipartFile[] files = docs201.getDocFiles();
		List<String> docFileUrls;
		String firstFileName = (files != null && files.length > 0) ? files[0].getOriginalFilename() : null;
		if (files == null || (files.length > 0 && (firstFileName == null || firstFileName.isEmpty()))) {
			// No new files selected — preserve whatever URLs the managed entity already holds
			docFileUrls = entityToSave.getDocFileUrls() != null ? entityToSave.getDocFileUrls() : new ArrayList<>();
		} else {
			docFileUrls = new ArrayList<>();
		    for (int i = 0; i < files.length; i++) {
		    	try {
		    		String name    = files[i].getOriginalFilename();
		    		if (name == null || name.isEmpty()) continue;
		    		String fileExt = name.substring(name.lastIndexOf('.'));
		    		String fileName = "emp_201file_" + empId + '_' + i + "_" + System.currentTimeMillis() + fileExt;
		    		FileDTO fileDTO = storageService.uploadFile(files[i], fileName);
		    		docFileUrls.add(fileDTO.getDownloadUri());
		    	} catch (Exception e) {
		    		log.error("Failed to upload 201 document file", e);
		    	}
		    }
		}
		entityToSave.setDocFileUrls(docFileUrls);
		docs201Repository.save(entityToSave);

		redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully Updated."));
		return "redirect:/201files/" + employee.getId() + "/" + employee.getEmpHashCode();
	}

}
