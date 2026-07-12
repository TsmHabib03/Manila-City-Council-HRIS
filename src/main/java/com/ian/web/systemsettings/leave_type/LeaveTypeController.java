package com.ian.web.systemsettings.leave_type;

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
import com.ian.web.systemsettings.common.SettingsDeleteUtil;

import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class LeaveTypeController {

    private final LeaveTypeRepository leaveTypeRepository;

    @GetMapping("/leave-types")
    public String getData(Model model) {
        model.addAttribute("listOfLeaveType", leaveTypeRepository.findAllByOrderByIdAsc());
        model.addAttribute("leaveType", new LeaveType());
        return "system-settings/leave-type/leave-type-list";
    }

    @PostMapping("/save-leave-type")
    @Transactional
    public String saveRecord(
            @Valid LeaveType leaveType
            , Errors errors
            , final RedirectAttributes redirect
            , Model model
            ) {
        if (errors.hasErrors()) {
            model.addAttribute("listOfLeaveType", leaveTypeRepository.findAllByOrderByIdAsc());
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
            return "system-settings/leave-type/leave-type-list";
        }

        boolean isNew = Objects.isNull(leaveType.getId());
        if (isNew && leaveTypeRepository.existsByLeaveTypeNameIgnoreCase(leaveType.getLeaveTypeName().trim())) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "That leave type already exists."));
            return "redirect:/leave-types";
        }
        leaveType.setLeaveTypeName(leaveType.getLeaveTypeName().trim());
        leaveTypeRepository.save(leaveType);

        redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
        return "redirect:/leave-types";
    }

    @PostMapping("/delete-leave-type/{id}")
    public String deleteLeaveType(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        if (!isAdmin(request)) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
            return "redirect:/leave-types";
        }
        redirect.addFlashAttribute("uxmessage",
            SettingsDeleteUtil.tryDelete(() -> leaveTypeRepository.deleteById(id), "Leave Type"));
        return "redirect:/leave-types";
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object actorObj = request.getSession().getAttribute("actorObj");
        return actorObj instanceof com.ian.web.employee.Employee
            && "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
    }
}
