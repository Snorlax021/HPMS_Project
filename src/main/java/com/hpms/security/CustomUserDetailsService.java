package com.hpms.security;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Custom UserDetailsService implementation for HPMS.
 * 
 * This service loads user details for authentication.
 * Currently uses in-memory storage, but can be extended to use database.
 * 
 * Default users:
 * - admin/admin123 (ROLE_ADMIN)
 * - doctor/doctor123 (ROLE_DOCTOR)
 * - staff/staff123 (ROLE_STAFF)
 * - patient/patient123 (ROLE_PATIENT)
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    // In-memory user store - will be replaced with database repository
    private final Map<String, UserInfo> users = new HashMap<>();

    public CustomUserDetailsService() {
        // Initialize default users (for development/demo)
        // Using BCrypt encoded passwords
        // All users have password: admin123, doctor123, staff123, patient123
        
        users.put("admin", new UserInfo("admin", 
            "$2a$10$/Ha/oTVRaV0rn0CeGijOsueggK7FV18QgtiCwOIyPlMlXX6R8aVxG", 
            "ROLE_ADMIN"));
        
        users.put("doctor", new UserInfo("doctor", 
            "$2a$10$8kfHfTSG9Lp8lQXGnGhDXuqnZlqrJnw4FGj2NoLxKWY.cKCz4gvDm", 
            "ROLE_DOCTOR"));
        
        users.put("staff", new UserInfo("staff", 
            "$2a$10$L9J.rp6FU8MwqQY.qC7XP.1lU1YV1iRZ4bMz8Nn8HwNxr4jRxQqPu", 
            "ROLE_STAFF"));
        
        users.put("patient", new UserInfo("patient", 
            "$2a$10$xRqzVj3DjN6kF8zLF7GqO.JF7cqBZFjwKTJHxQqPuNqvYZ8gzRqPm", 
            "ROLE_PATIENT"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserInfo userInfo = users.get(username.toLowerCase());
        
        if (userInfo == null) {
            throw new UsernameNotFoundException("User not found: " + username);
        }

        return User.builder()
                .username(userInfo.username)
                .password(userInfo.password)
                .authorities(Collections.singletonList(
                    new SimpleGrantedAuthority(userInfo.role)))
                .build();
    }

    /**
     * Add or update user (for registration or admin management)
     */
    public void saveUser(String username, String encodedPassword, String role) {
        users.put(username.toLowerCase(), new UserInfo(username, encodedPassword, role));
    }

    /**
     * Check if user exists
     */
    public boolean userExists(String username) {
        return users.containsKey(username.toLowerCase());
    }

    /**
     * Simple class to hold user information
     */
    private static class UserInfo {
        String username;
        String password;
        String role;

        UserInfo(String username, String password, String role) {
            this.username = username;
            this.password = password;
            this.role = role;
        }
    }
}
