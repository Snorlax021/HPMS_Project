# Hospital Patient Management System (HPMS)

A comprehensive hospital patient management system built with Spring Boot, featuring JWT-based authentication, role-based access control, and RESTful APIs.

## Features

- **JWT-based Authentication**: Secure token-based authentication system
- **Role-Based Access Control**: Support for ADMIN, DOCTOR, STAFF, and PATIENT roles
- **RESTful API**: Clean REST endpoints for all operations
- **Database Support**: H2 in-memory database (development) with easy migration to production databases
- **Global Exception Handling**: Consistent error responses across the application
- **Input Validation**: Comprehensive validation using Jakarta Bean Validation
- **API Documentation**: Swagger/OpenAPI integration for interactive API docs
- **Security**: BCrypt password encoding and secure JWT token management

## Technology Stack

- **Java 17**
- **Spring Boot 3.2.0**
- **Spring Security** (JWT-based)
- **Spring Data JPA**
- **H2 Database** (development)
- **Maven** (build tool)
- **Lombok** (reduce boilerplate)
- **MapStruct** (DTO mapping)
- **Swagger/OpenAPI** (API documentation)

## Prerequisites

- Java 17 or higher
- Maven 3.6 or higher

## Building the Project

```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package

# Clean build (skipping tests)
mvn clean package -DskipTests
```

## Running the Application

### Option 1: Using Maven

```bash
mvn spring-boot:run
```

### Option 2: Using the JAR file

```bash
# First build the JAR
mvn clean package

# Then run it
java -jar target/hpms-system-1.0.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080/api`

## Default Users

The application comes with pre-configured demo users:

| Username | Password   | Role    |
|----------|------------|---------|
| admin    | admin123   | ADMIN   |
| doctor   | doctor123  | DOCTOR  |
| staff    | staff123   | STAFF   |
| patient  | patient123 | PATIENT |

⚠️ **Important**: Change these credentials in production!

## API Documentation

Once the application is running, access the Swagger UI at:

```
http://localhost:8080/swagger-ui.html
```

API JSON documentation available at:

```
http://localhost:8080/api-docs
```

## H2 Database Console

For development, you can access the H2 database console at:

```
http://localhost:8080/api/h2-console
```

Connection details:
- JDBC URL: `jdbc:h2:mem:hpms_db`
- Username: `sa`
- Password: (leave empty)

## API Endpoints

### Authentication

- `POST /api/auth/login` - User login (returns JWT token)
- `GET /api/auth/health` - Health check

### Example Login Request

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "admin123"
  }'
```

### Example Response

```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ROLE_ADMIN",
  "message": "Authentication successful"
}
```

### Using the JWT Token

Include the token in subsequent requests:

```bash
curl -X GET http://localhost:8080/api/some-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Project Structure

```
HPMS_Project/
├── src/
│   ├── main/
│   │   ├── java/com/hpms/
│   │   │   ├── Application.java           # Main entry point
│   │   │   ├── config/                    # Configuration classes
│   │   │   │   └── SecurityConfig.java    # Security configuration
│   │   │   ├── controller/                # REST controllers
│   │   │   │   └── AuthController.java    # Authentication endpoints
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   ├── exception/                 # Custom exceptions
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── security/                  # Security components
│   │   │       ├── JwtUtil.java          # JWT utilities
│   │   │       ├── JwtAuthenticationFilter.java
│   │   │       └── CustomUserDetailsService.java
│   │   └── resources/
│   │       ├── application.properties     # Configuration
│   │       ├── schema.sql                # Database schema
│   │       └── data.sql                  # Initial data
│   └── test/                             # Test files
├── HPMS/                                 # Legacy UI code
│   └── src/                              # Java Swing UI
├── pom.xml                               # Maven configuration
└── README.md                             # This file
```

## Configuration

Key configuration properties in `application.properties`:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:h2:mem:hpms_db

# JWT
jwt.secret=your-secret-key
jwt.expiration=86400000  # 24 hours

# Security
# Configure as needed for your environment
```

## Security Notes

1. **JWT Secret**: The default JWT secret should be changed in production
2. **Passwords**: Default passwords are for development only
3. **HTTPS**: Use HTTPS in production environments
4. **Database**: Use a production database (PostgreSQL, MySQL) instead of H2

## Development

### Adding New Endpoints

1. Create a controller in `src/main/java/com/hpms/controller/`
2. Define DTOs in `src/main/java/com/hpms/dto/`
3. Implement business logic in services
4. Add appropriate security annotations

### Custom Exceptions

Extend the exception handling in `GlobalExceptionHandler.java` for custom error responses.

## Future Enhancements

- [ ] Additional REST controllers for Patient, Appointment, Billing
- [ ] WebSocket support for real-time notifications
- [ ] Email integration for appointment reminders
- [ ] Advanced reporting and analytics
- [ ] Integration with external medical systems
- [ ] Mobile app support

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is provided for educational purposes.

## Support

For issues and questions, please create an issue in the GitHub repository.
