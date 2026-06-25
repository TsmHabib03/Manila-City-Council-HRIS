package com.ian.web.fileupload;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
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
            ,HttpServletRequest request
            ) throws Exception {
        log.info("REST request to upload file");
        //upload files
        Employee employee = this.employeeRepository.findById(employeeId)
                .orElseGet(Employee::new);

        String fileName = "employee_" + employee.getId() + file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf("."));
        FileDTO fileDTO = this.storageService.uploadFile(file, fileName);
        employee.setProfilePhoto(fileDTO.getDownloadUri());
        this.employeeRepository.save(employee);

        // Refresh Spring Security principal so sidebar photo updates on next page load
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Employee) {
            Employee principal = (Employee) auth.getPrincipal();
            if (principal.getId() == employee.getId()) {
                Employee fresh = this.employeeRepository.findById(employeeId).orElse(employee);
                Authentication newAuth = new UsernamePasswordAuthenticationToken(fresh, auth.getCredentials(), auth.getAuthorities());
                SecurityContext ctx = SecurityContextHolder.createEmptyContext();
                ctx.setAuthentication(newAuth);
                SecurityContextHolder.setContext(ctx);
                HttpSession session = request.getSession(false);
                if (session != null) {
                    session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, ctx);
                    session.setAttribute("actorObj", fresh);
                }
            }
        }

        return new ResponseEntity<>(fileDTO, null, HttpStatus.OK);
    }

    

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Object> downloadFile(@PathVariable String fileName, HttpServletRequest request) throws Exception {
        return this.storageService.downloadFile(fileName, request);
    }
}