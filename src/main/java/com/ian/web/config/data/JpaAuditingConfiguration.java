//package com.ian.web.config.data;
//
//import java.util.Optional;
//
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.data.domain.AuditorAware;
//import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
//import org.springframework.security.core.context.SecurityContextHolder;
//
//@Configuration
//@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
//public class JpaAuditingConfiguration {
//
//    @Bean
//    public AuditorAware<String> auditorProvider() {
//
//        return new AuditorAware<String>() {
//        	@Override
//        	public Optional<String> getCurrentAuditor() {
//        		return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication().getName());
//        	}
//		};
//    }
//}

package com.ian.web.config.data;

import java.util.Optional;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfiguration {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    public static class AuditorAwareImpl implements AuditorAware<String> {
        @Override
        public Optional<String> getCurrentAuditor() {
            // Get the current authentication object from the security context
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Check if there is no authentication, if it's not authenticated,
            // or if the user is the default anonymous user.
            if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication instanceof AnonymousAuthenticationToken) {
                
                // For scripts, system processes, or unauthenticated access,
                // return a default system user name.
                return Optional.of("system-migration");
            }

            // If there is a logged-in user, return their username.
            return Optional.of(authentication.getName());
        }
    }
}
