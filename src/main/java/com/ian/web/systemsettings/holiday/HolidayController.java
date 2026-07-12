package com.ian.web.systemsettings.holiday;

import java.util.List;

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
public class HolidayController {

    private static final List<String> HOLIDAY_TYPE_LIST = List.of(
            Holiday.TYPE_REGULAR, Holiday.TYPE_SPECIAL_NON_WORKING,
            Holiday.TYPE_SPECIAL_WORKING, Holiday.TYPE_LOCAL);

    private final HolidayRepository holidayRepository;

    @GetMapping("/holidays")
    public String getData(Model model) {
        model.addAttribute("listOfHoliday", holidayRepository.findAllByOrderByHolidayDateAsc());
        model.addAttribute("holiday", new Holiday());
        model.addAttribute("holidayTypeList", HOLIDAY_TYPE_LIST);
        return "system-settings/holiday/holiday-list";
    }

    @PostMapping("/save-holiday")
    @Transactional
    public String saveRecord(
            @Valid Holiday holiday
            , Errors errors
            , final RedirectAttributes redirect
            , Model model
            ) {
        if (errors.hasErrors()) {
            model.addAttribute("listOfHoliday", holidayRepository.findAllByOrderByHolidayDateAsc());
            model.addAttribute("holidayTypeList", HOLIDAY_TYPE_LIST);
            model.addAttribute("uxmessage", new UXMessage("ERROR", "Please check items marked in red."));
            return "system-settings/holiday/holiday-list";
        }

        holiday.setHolidayName(holiday.getHolidayName().trim());
        holidayRepository.save(holiday);

        redirect.addFlashAttribute("uxmessage", new UXMessage("SUCCESS", "Record successfully saved."));
        return "redirect:/holidays";
    }

    @PostMapping("/delete-holiday/{id}")
    public String deleteHoliday(@PathVariable Long id, HttpServletRequest request, RedirectAttributes redirect) {
        if (!isAdmin(request)) {
            redirect.addFlashAttribute("uxmessage", new UXMessage("ERROR", "Access denied."));
            return "redirect:/holidays";
        }
        redirect.addFlashAttribute("uxmessage",
            SettingsDeleteUtil.tryDelete(() -> holidayRepository.deleteById(id), "Holiday"));
        return "redirect:/holidays";
    }

    private boolean isAdmin(HttpServletRequest request) {
        Object actorObj = request.getSession().getAttribute("actorObj");
        return actorObj instanceof com.ian.web.employee.Employee
            && "ROLE_ADMIN".equals(((com.ian.web.employee.Employee) actorObj).getUserType());
    }
}
