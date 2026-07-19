package com.ian.web.config.security;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ian.web.employee.Employee;
import com.ian.web.employee.EmployeeRepository;

import lombok.RequiredArgsConstructor;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class DevUserBootstrap implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        ensureLogin("admin", "manila@123", Roles.ADMIN);
        ensureLogin("hr_user", "hr_pass", Roles.HR);
        ensureLogin("emp_user", "emp_pass", Roles.EMPLOYEE);
        // CR 016 v2: leave workflow actor accounts. Seeded with a non-active
        // employee status so they are never swept into the year-end deduction.
        ensureLogin("supervisor", "sup_pass", Roles.SUPERVISOR, "N/A");
        ensureLogin("council_sec", "council_pass", Roles.COUNCIL, "N/A");
        ensureLogin("vice_mayor", "vm_pass", Roles.VICEMAYOR, "N/A");
    }

    private void ensureLogin(String username, String rawPassword, String role) {
        ensureLogin(username, rawPassword, role, "A");
    }

    private void ensureLogin(String username, String rawPassword, String role, String status) {
        Employee employee = employeeRepository.findByUsername(username)
                .orElse(null);

        boolean isNew = (employee == null);
        if (employee == null) {
            employee = new Employee();
            employee.setUsername(username);
            employee.setEmpHashCode("dev-" + UUID.randomUUID().toString());
            employee.setFirstName(username);
            employee.setLastName("User");
            employee.setStatus(status);
        }

        employee.setUserType(role);
        if (isNew) {
            employee.setPassword(passwordEncoder.encode(rawPassword));
        }

        employeeRepository.save(employee);
    }
}
