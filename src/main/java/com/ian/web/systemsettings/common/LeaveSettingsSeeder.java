package com.ian.web.systemsettings.common;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.ian.web.employee.leave.LeaveConstants;
import com.ian.web.systemsettings.holiday.Holiday;
import com.ian.web.systemsettings.holiday.HolidayRepository;
import com.ian.web.systemsettings.leave_type.LeaveType;
import com.ian.web.systemsettings.leave_type.LeaveTypeRepository;

import lombok.RequiredArgsConstructor;

/**
 * One-time seeding of the leave settings tables so the admin starts with the
 * CS Form 6 leave types and the current Manila holiday calendar instead of
 * empty lists. Runs on every boot but only inserts when a table is empty, so
 * admin edits and deletions are never overwritten.
 */
@Component
@RequiredArgsConstructor
public class LeaveSettingsSeeder implements CommandLineRunner {

    private final LeaveTypeRepository leaveTypeRepository;
    private final HolidayRepository holidayRepository;

    @Override
    public void run(String... args) {
        seedLeaveTypes();
        seedHolidays();
    }

    private void seedLeaveTypes() {
        if (leaveTypeRepository.count() > 0) {
            return;
        }
        for (String name : LeaveConstants.LEAVE_TYPE_LIST) {
            leaveTypeRepository.save(LeaveType.builder().leaveTypeName(name).build());
        }
    }

    private void seedHolidays() {
        if (holidayRepository.count() > 0) {
            return;
        }
        // 2026 Philippine holidays; movable religious dates should be adjusted
        // by the admin once the yearly proclamation is issued.
        holiday("New Year's Day", LocalDate.of(2026, 1, 1), Holiday.TYPE_REGULAR, null);
        holiday("Chinese New Year", LocalDate.of(2026, 2, 17), Holiday.TYPE_SPECIAL_NON_WORKING, null);
        holiday("Eid'l Fitr (Feast of Ramadhan)", LocalDate.of(2026, 3, 20), Holiday.TYPE_REGULAR,
                "Movable date - confirm with the yearly proclamation");
        holiday("Maundy Thursday", LocalDate.of(2026, 4, 2), Holiday.TYPE_REGULAR, null);
        holiday("Good Friday", LocalDate.of(2026, 4, 3), Holiday.TYPE_REGULAR, null);
        holiday("Black Saturday", LocalDate.of(2026, 4, 4), Holiday.TYPE_SPECIAL_NON_WORKING, null);
        holiday("Araw ng Kagitingan", LocalDate.of(2026, 4, 9), Holiday.TYPE_REGULAR, null);
        holiday("Labor Day", LocalDate.of(2026, 5, 1), Holiday.TYPE_REGULAR, null);
        holiday("Eid'l Adha (Feast of Sacrifice)", LocalDate.of(2026, 5, 27), Holiday.TYPE_REGULAR,
                "Movable date - confirm with the yearly proclamation");
        holiday("Independence Day", LocalDate.of(2026, 6, 12), Holiday.TYPE_REGULAR, null);
        holiday("Araw ng Maynila (Manila Day)", LocalDate.of(2026, 6, 24), Holiday.TYPE_LOCAL,
                "Foundation day of the City of Manila");
        holiday("Ninoy Aquino Day", LocalDate.of(2026, 8, 21), Holiday.TYPE_SPECIAL_NON_WORKING, null);
        holiday("National Heroes Day", LocalDate.of(2026, 8, 31), Holiday.TYPE_REGULAR, "Last Monday of August");
        holiday("All Saints' Day", LocalDate.of(2026, 11, 1), Holiday.TYPE_SPECIAL_NON_WORKING, null);
        holiday("Bonifacio Day", LocalDate.of(2026, 11, 30), Holiday.TYPE_REGULAR, null);
        holiday("Feast of the Immaculate Conception", LocalDate.of(2026, 12, 8), Holiday.TYPE_SPECIAL_NON_WORKING, null);
        holiday("Christmas Day", LocalDate.of(2026, 12, 25), Holiday.TYPE_REGULAR, null);
        holiday("Rizal Day", LocalDate.of(2026, 12, 30), Holiday.TYPE_REGULAR, null);
        holiday("Last Day of the Year", LocalDate.of(2026, 12, 31), Holiday.TYPE_SPECIAL_NON_WORKING, null);
    }

    private void holiday(String name, LocalDate date, String type, String remarks) {
        holidayRepository.save(Holiday.builder()
                .holidayName(name).holidayDate(date).holidayType(type).remarks(remarks).build());
    }
}
