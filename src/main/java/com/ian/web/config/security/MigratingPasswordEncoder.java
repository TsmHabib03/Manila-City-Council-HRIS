package com.ian.web.config.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Supports BCrypt-encoded passwords and, as a one-time migration path,
 * plain-text legacy passwords. upgradeEncoding() returns true for plain-text
 * passwords so Spring Security automatically re-encodes them with BCrypt on
 * next successful login. Once all users have logged in after migration, this
 * plain-text fallback can be removed.
 *
 * TODO: Remove the plain-text fallback after confirming all legacy passwords
 * have been upgraded (check for any rows in employee table where password
 * does not start with '$2').
 */
public class MigratingPasswordEncoder implements PasswordEncoder {

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder(11);

    @Override
    public String encode(CharSequence rawPassword) {
        return bcrypt.encode(rawPassword);
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isEmpty()) {
            return false;
        }
        if (encodedPassword.startsWith("$2a$") || encodedPassword.startsWith("$2b$") || encodedPassword.startsWith("$2y$")) {
            return bcrypt.matches(rawPassword, encodedPassword);
        }
        // Plain-text fallback for legacy accounts — upgradeEncoding() ensures
        // Spring Security re-encodes this password with BCrypt on next login.
        return rawPassword.toString().equals(encodedPassword);
    }

    @Override
    public boolean upgradeEncoding(String encodedPassword) {
        return encodedPassword != null && !encodedPassword.startsWith("$2");
    }
}
