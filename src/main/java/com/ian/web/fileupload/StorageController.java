package com.ian.web.fileupload;

import javax.servlet.http.HttpServletRequest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller 
@AllArgsConstructor
@RequestMapping("/file")
public class StorageController {

    private StorageService storageService;
    private EmployeeRepository employeeRepository;

    @PostMapping({"/upload","/upload/profilePhoto"})
    @Transactional
    public ResponseEntity<FileDTO> uploadFile(
            @RequestParam("profilePhoto") MultipartFile file
            ,@RequestParam("employeeId") long employeeId
            ) throws Exception {
        log.info("REST request to upload file");
        //upload files
        Employee employee = this.employeeRepository.findById(employeeId)
                .orElseGet(Employee::new);

        String fileName = "employee_" + employee.getId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        FileDTO fileDTO = this.storageService.uploadFile(file, fileName);
        employee.setProfilePhoto(fileDTO.getDownloadUri());
        this.employeeRepository.save(employee);
        return new ResponseEntity<>(fileDTO, null, HttpStatus.OK);
    }

    

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Object> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws Exception {
        return this.storageService.downloadFile(fileName, request);
    }
}