package com.hpms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for User entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    
    private String id;
    
    @NotBlank(message = "Username is required")
    private String username;
    
    @NotBlank(message = "Role is required")
    private String role;
    
    private String createdAt;
    
    // Password is intentionally omitted for security
}
