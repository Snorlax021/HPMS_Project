package com.hpms.controller;

import com.hpms.dto.AppointmentDTO;
import com.hpms.exception.ResourceNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST Controller for Appointment management.
 * 
 * Provides operations for scheduling, viewing, and managing appointments.
 * Requires authentication and appropriate role permissions.
 */
@RestController
@RequestMapping("/appointments")
@Tag(name = "Appointment Management", description = "APIs for managing patient appointments")
@SecurityRequirement(name = "Bearer Authentication")
public class AppointmentController {

    // TODO: Inject AppointmentService when it's converted to Spring bean
    // @Autowired
    // private AppointmentService appointmentService;

    /**
     * Get all appointments
     * Accessible by ADMIN, DOCTOR, and STAFF
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    @Operation(summary = "Get all appointments", description = "Retrieve a list of all appointments")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        // TODO: Implement with AppointmentService
        List<AppointmentDTO> appointments = new ArrayList<>();
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointment by ID
     * Accessible by ADMIN, DOCTOR, STAFF, and the patient who owns it
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF', 'PATIENT')")
    @Operation(summary = "Get appointment by ID", description = "Retrieve a specific appointment by its ID")
    public ResponseEntity<AppointmentDTO> getAppointmentById(@PathVariable String id) {
        // TODO: Implement with AppointmentService
        throw new ResourceNotFoundException("Appointment", "id", id);
    }

    /**
     * Create new appointment
     * Accessible by ADMIN, STAFF, and PATIENT (for self-booking)
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    @Operation(summary = "Create new appointment", description = "Schedule a new appointment")
    public ResponseEntity<AppointmentDTO> createAppointment(@Valid @RequestBody AppointmentDTO appointmentDTO) {
        // TODO: Implement with AppointmentService
        return ResponseEntity.status(HttpStatus.CREATED).body(appointmentDTO);
    }

    /**
     * Update appointment
     * Accessible by ADMIN and STAFF
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update appointment", description = "Update an existing appointment")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable String id,
            @Valid @RequestBody AppointmentDTO appointmentDTO) {
        // TODO: Implement with AppointmentService
        throw new ResourceNotFoundException("Appointment", "id", id);
    }

    /**
     * Cancel appointment
     * Accessible by ADMIN, STAFF, and the patient who owns it
     */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF', 'PATIENT')")
    @Operation(summary = "Cancel appointment", description = "Cancel an existing appointment")
    public ResponseEntity<AppointmentDTO> cancelAppointment(@PathVariable String id) {
        // TODO: Implement with AppointmentService - call appointmentService.cancel(id)
        throw new ResourceNotFoundException("Appointment", "id", id);
    }

    /**
     * Complete appointment
     * Accessible by ADMIN, DOCTOR, and STAFF
     */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    @Operation(summary = "Complete appointment", description = "Mark an appointment as completed")
    public ResponseEntity<AppointmentDTO> completeAppointment(@PathVariable String id) {
        // TODO: Implement with AppointmentService - call appointmentService.complete(id)
        throw new ResourceNotFoundException("Appointment", "id", id);
    }

    /**
     * Get appointments by patient ID
     * Accessible by ADMIN, DOCTOR, STAFF, and the patient themselves
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF', 'PATIENT')")
    @Operation(summary = "Get appointments by patient", description = "Retrieve all appointments for a specific patient")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByPatient(@PathVariable String patientId) {
        // TODO: Implement with AppointmentService
        List<AppointmentDTO> appointments = new ArrayList<>();
        return ResponseEntity.ok(appointments);
    }

    /**
     * Get appointments by status
     * Accessible by ADMIN, DOCTOR, and STAFF
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    @Operation(summary = "Get appointments by status", description = "Retrieve appointments filtered by status")
    public ResponseEntity<List<AppointmentDTO>> getAppointmentsByStatus(@PathVariable String status) {
        // TODO: Implement with AppointmentService
        List<AppointmentDTO> appointments = new ArrayList<>();
        return ResponseEntity.ok(appointments);
    }

    /**
     * Delete appointment
     * Accessible by ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete appointment", description = "Remove an appointment from the system")
    public ResponseEntity<Void> deleteAppointment(@PathVariable String id) {
        // TODO: Implement with AppointmentService
        return ResponseEntity.noContent().build();
    }
}
