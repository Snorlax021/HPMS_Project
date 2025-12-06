package com.hpms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application entry point for the Hospital Patient Management System (HPMS).
 * 
 * This is a Spring Boot application that provides:
 * - RESTful API endpoints for patient, appointment, and billing management
 * - JWT-based authentication and authorization
 * - Role-based access control (ADMIN, DOCTOR, STAFF, PATIENT)
 * - In-memory H2 database for development (can be configured for production DB)
 * 
 * To run: mvn spring-boot:run
 * API docs available at: http://localhost:8080/swagger-ui.html
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
