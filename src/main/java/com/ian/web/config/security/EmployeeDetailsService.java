package com.ian.web.config.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import com.ian.web.employee.EmployeeRepository;

@Service
public class EmployeeDetailsService implements UserDetailsService {

    private EmployeeRepository employeeRepository;

    public EmployeeDetailsService(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        if (username != null) {
            return this.employeeRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Username not found."));
        }
        throw new UsernameNotFoundException("Username not found.");
    }
}
