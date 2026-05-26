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
