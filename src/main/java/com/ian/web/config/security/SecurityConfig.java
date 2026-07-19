package com.ian.web.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

	private static final String[] PUBLIC = new String[]
			{"/login","/logout","/error"};
	private static final String[] ASSETS = new String[]
			{"/assets/**","/global_assets/**",
					"/admin_js/**","/parent_css/**","/parent_js/**",
					"/js/*","/js/**","/css/*","/css/**","/images/*","/images/**"};

	// Leave-module endpoints that mutate records or expose the admin roster/views.
	// The UI already hides these from employees (showMode != 'EMPLOYEE'); this enforces
	// it server-side so an authenticated ROLE_EMPLOYEE cannot reach them directly.
	// /addLeaveApplication and /updateLeaveApplication are intentionally NOT listed here:
	// employees may file their own leave application through those same URLs, same as
	// /addEmployee et al. for PDS self-service. LeaveController.saveLeaveApplication does
	// the in-method own-record + admin-field-reset guard (mirrors EmployeeController.saveEmployee).
	// Employee self-service also uses /my-leaves/** and the hash-gated PDF endpoints, which
	// stay at .authenticated().
	// /cancelMyLeave/** and /appealMyLeave/** are intentionally NOT listed here (CR 016):
	// employees cancel/appeal their own applications; the controller does the owner guard.
	private static final String[] LEAVE_ADMIN = new String[] {
			"/leaves", "/leaves/**",
			"/deleteLeaveApplication/**",
			"/addLeaveCardEntry", "/updateLeaveCardEntry", "/deleteLeaveCardEntry/**",
			"/postLeaveAccrual/**",
			"/holidays", "/save-holiday", "/delete-holiday/**",
			"/leave-types", "/save-leave-type", "/delete-leave-type/**",
			"/leave-signatories", "/saveLeaveSignatories",
			"/leave-tracker", "/leave-list/**",
			"/leave-workflow/**", "/leave-applications", "/leave-pending-list",
			"/leave-year-end/**", "/leaveVerificationReceiptPdf/**"};

	// CR Request ID 015 modules: Archive (SALN / Resigned / past Leaves files)
	// and Training & Seminar are HR records management — staff only.
	private static final String[] ARCHIVE_ADMIN = new String[] {
			"/archive/**", "/trainings", "/saveTraining", "/deleteTraining/**"};

	private final UserDetailsService userDetailsService;

	public SecurityConfig(UserDetailsService userDetailsService) {
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		String landingURL = "/dashboard";
		http
			.csrf()
				.ignoringAntMatchers("/api/**", "/saveServiceRecordRequest", "/process-clearance/**")
				.and()
			.authorizeRequests()
				.antMatchers(PUBLIC).permitAll()
				.antMatchers(LEAVE_ADMIN).hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")
				.antMatchers(ARCHIVE_ADMIN).hasAnyAuthority("ROLE_ADMIN", "ROLE_HR")
				.anyRequest().authenticated()
				.and()
			.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/login")
				.defaultSuccessUrl(landingURL, true)
				.failureUrl("/login?error")
				.and()
			.logout()
			;
	}

	@Override
	public void configure(WebSecurity web) {
		web.ignoring().antMatchers(ASSETS);
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new MigratingPasswordEncoder();
	}

}
