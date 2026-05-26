package com.ian.web.changepassword;

import javax.validation.Valid;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import org.springframework.security.crypto.password.PasswordEncoder;

import com.ian.web.common.model.UXMessage;
import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;


@Controller
@RequiredArgsConstructor
public class ChangePasswordController {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/change-password/{employeeId}")
    public String getPassword(@PathVariable("employeeId") Long id, Model model) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        model.addAttribute("employee", employee);
        model.addAttribute("changePassword", new ChangePassword());
        return "/changepassword/change-password";
    }

    @PostMapping("/save-change-password/{employeeId}")
    public String savePassword(@Valid ChangePassword changePassword,
                               @PathVariable("employeeId") Long id,
                               Errors errors,
                               final RedirectAttributes redirect) {

        if(changePassword.getConfirmPassword().isBlank() || changePassword.getNewPassword().isBlank() || changePassword.getOldPassword().isBlank()){
            redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Please fill up empty field."));
            return "redirect:/change-password/" + id;
        }else{
            Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

            if (changePassword.getConfirmPassword().equals(changePassword.getNewPassword())
                    && passwordEncoder.matches(changePassword.getOldPassword(), employee.getPassword())) {
                employee.setPassword(passwordEncoder.encode(changePassword.getNewPassword()));
                employeeRepository.save(employee);
                redirect.addFlashAttribute("msg", new UXMessage("EDIT-SUCCESS", "Record Successfully saved."));
            } else if (!changePassword.getConfirmPassword().equals(changePassword.getNewPassword())) {
                redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Mismatch new password to confirm password."));
            } else {
                redirect.addFlashAttribute("msg", new UXMessage("ERROR", "Invalid old password."));
            }
        }
        return "redirect:/change-password/" + id;
    }
}

