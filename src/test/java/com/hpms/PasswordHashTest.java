package com.hpms;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println("admin123 hash: " + encoder.encode("admin123"));
        System.out.println("Testing match: " + encoder.matches("admin123", "$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z2EQjINrXKR8sWE1UjESqYHu"));
    }
}
