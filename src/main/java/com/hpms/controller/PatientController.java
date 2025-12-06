package com.hpms.controller;

import com.hpms.dto.PatientDTO;
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
 * REST Controller for Patient management.
 * 
 * Provides CRUD operations for patient records.
 * Requires authentication and appropriate role permissions.
 */
@RestController
@RequestMapping("/patients")
@Tag(name = "Patient Management", description = "APIs for managing patient records")
@SecurityRequirement(name = "Bearer Authentication")
public class PatientController {

    // TODO: Inject PatientService when it's converted to Spring bean
    // @Autowired
    // private PatientService patientService;

    /**
     * Get all patients
     * Accessible by ADMIN, DOCTOR, and STAFF
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    @Operation(summary = "Get all patients", description = "Retrieve a list of all patients")
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        // TODO: Implement with PatientService
        List<PatientDTO> patients = new ArrayList<>();
        return ResponseEntity.ok(patients);
    }

    /**
     * Get patient by ID
     * Accessible by ADMIN, DOCTOR, STAFF, and the patient themselves
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF', 'PATIENT')")
    @Operation(summary = "Get patient by ID", description = "Retrieve a specific patient by their ID")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable String id) {
        // TODO: Implement with PatientService
        throw new ResourceNotFoundException("Patient", "id", id);
    }

    /**
     * Create new patient
     * Accessible by ADMIN and STAFF
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Create new patient", description = "Register a new patient in the system")
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO patientDTO) {
        // TODO: Implement with PatientService
        return ResponseEntity.status(HttpStatus.CREATED).body(patientDTO);
    }

    /**
     * Update patient
     * Accessible by ADMIN and STAFF
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
    @Operation(summary = "Update patient", description = "Update an existing patient's information")
    public ResponseEntity<PatientDTO> updatePatient(
            @PathVariable String id,
            @Valid @RequestBody PatientDTO patientDTO) {
        // TODO: Implement with PatientService
        throw new ResourceNotFoundException("Patient", "id", id);
    }

    /**
     * Delete patient
     * Accessible by ADMIN only
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete patient", description = "Remove a patient from the system")
    public ResponseEntity<Void> deletePatient(@PathVariable String id) {
        // TODO: Implement with PatientService
        return ResponseEntity.noContent().build();
    }

    /**
     * Search patients by name
     * Accessible by ADMIN, DOCTOR, and STAFF
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'DOCTOR', 'STAFF')")
    @Operation(summary = "Search patients", description = "Search patients by name")
    public ResponseEntity<List<PatientDTO>> searchPatients(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName) {
        // TODO: Implement with PatientService
        List<PatientDTO> patients = new ArrayList<>();
        return ResponseEntity.ok(patients);
    }
}
