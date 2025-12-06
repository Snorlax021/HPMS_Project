package com.hpms.controller;

import com.hpms.dto.AuthRequestDTO;
import com.hpms.dto.AuthResponseDTO;
import com.hpms.exception.AuthenticationException;
import com.hpms.security.CustomUserDetailsService;
import com.hpms.security.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication Controller for login and token generation.
 * 
 * Endpoints:
 * - POST /api/auth/login - Authenticate user and generate JWT token
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    /**
     * Login endpoint - authenticate user and return JWT token
     * 
     * @param authRequest Login credentials (username and password)
     * @return JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody AuthRequestDTO authRequest) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    authRequest.getUsername(),
                    authRequest.getPassword()
                )
            );

            // Load user details
            UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getUsername());
            
            // Extract role
            String role = userDetails.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

            // Generate JWT token with role
            String token = jwtUtil.generateTokenWithRole(userDetails.getUsername(), role);

            // Create response
            AuthResponseDTO response = new AuthResponseDTO(
                token,
                userDetails.getUsername(),
                role
            );

            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            throw new AuthenticationException("Invalid username or password");
        } catch (Exception e) {
            throw new AuthenticationException("Authentication failed: " + e.getMessage());
        }
    }

    /**
     * Health check endpoint
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("HPMS Authentication Service is running");
    }
}
