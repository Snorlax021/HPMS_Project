package com.hpms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Appointment entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentDTO {
    
    private String id;
    
    @NotBlank(message = "Patient ID is required")
    private String patientId;
    
    private String staffId;
    
    @NotNull(message = "Scheduled time is required")
    private LocalDateTime scheduledAt;
    
    private String reason;
    
    private String status;
    
    private String createdAt;
}
