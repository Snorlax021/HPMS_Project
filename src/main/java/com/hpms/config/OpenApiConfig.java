package com.hpms.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI/Swagger configuration for API documentation.
 * 
 * Accessible at: http://localhost:8080/swagger-ui.html
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Hospital Patient Management System API",
        version = "1.0.0",
        description = "REST API for managing hospital patients, appointments, billing, and user authentication. " +
                     "This system provides comprehensive healthcare management capabilities with role-based access control.",
        contact = @Contact(
            name = "HPMS Development Team",
            email = "support@hpms.com"
        )
    ),
    servers = {
        @Server(
            description = "Local Development Server",
            url = "http://localhost:8080/api"
        )
    }
)
@SecurityScheme(
    name = "Bearer Authentication",
    description = "JWT authentication token. Use the /auth/login endpoint to obtain a token.",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class OpenApiConfig {
    // Configuration is done through annotations
}
