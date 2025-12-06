# Integration Guide: Connecting Existing Services to Spring Boot

This guide explains how to integrate the existing HPMS services (in `/HPMS/src`) with the new Spring Boot REST API.

## Overview

The existing codebase has:
- **Models**: `User`, `Patient`, `Doctor`, `Appointment`, `Billing`, etc.
- **Repositories**: `InMemoryRepository<ID, T>` - generic in-memory storage
- **Services**: `UserService`, `PatientService`, `AppointmentService`, `BillingService`
- **UI**: Java Swing desktop application

The new Spring Boot infrastructure adds:
- REST API with JWT authentication
- Database persistence with JPA/H2
- Role-based security
- API documentation with Swagger

## Integration Steps

### Step 1: Convert Models to JPA Entities

Add JPA annotations to existing model classes:

```java
// Example: Patient.java
package Model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "patients")
public class Patient {
    @Id
    private String id;
    
    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;
    
    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;
    
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    
    // ... rest of fields with @Column annotations
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    // Constructors, getters, setters...
}
```

### Step 2: Create JPA Repositories

Create Spring Data JPA repositories:

```java
package com.hpms.repository;

import Model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PatientRepository extends JpaRepository<Patient, String> {
    // Spring Data JPA will implement basic CRUD automatically
    
    // Custom query methods
    List<Patient> findByLastNameContainingIgnoreCase(String lastName);
    Optional<Patient> findByEmail(String email);
}
```

### Step 3: Convert Services to Spring Components

Add Spring annotations to existing services:

```java
package Service;

import Model.Patient;
import com.hpms.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

@Service
@Transactional
public class PatientService {
    
    @Autowired
    private PatientRepository patientRepository;  // Replaces InMemoryRepository
    
    public Patient createPatient(String firstName, String lastName, LocalDate dob,
                                String gender, String phone, String email, String address) {
        Patient p = new Patient(firstName, lastName, dob, gender, phone, email, address);
        return patientRepository.save(p);
    }
    
    public Optional<Patient> findById(String id) { 
        return patientRepository.findById(id); 
    }
    
    public Collection<Patient> listAll() { 
        return patientRepository.findAll(); 
    }
    
    public boolean deletePatient(String id) { 
        if (patientRepository.existsById(id)) {
            patientRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
```

### Step 4: Wire Services into Controllers

Update controllers to use services:

```java
package com.hpms.controller;

import Service.PatientService;
import Model.Patient;
import com.hpms.dto.PatientDTO;
import com.hpms.mapper.PatientMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;
    
    @Autowired
    private PatientMapper patientMapper;  // MapStruct mapper

    @GetMapping
    public ResponseEntity<List<PatientDTO>> getAllPatients() {
        List<Patient> patients = (List<Patient>) patientService.listAll();
        List<PatientDTO> dtos = patients.stream()
            .map(patientMapper::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PatientDTO> getPatientById(@PathVariable String id) {
        return patientService.findById(id)
            .map(patientMapper::toDTO)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }

    @PostMapping
    public ResponseEntity<PatientDTO> createPatient(@Valid @RequestBody PatientDTO dto) {
        Patient patient = patientService.createPatient(
            dto.getFirstName(),
            dto.getLastName(),
            dto.getDateOfBirth(),
            dto.getGender(),
            dto.getPhone(),
            dto.getEmail(),
            dto.getAddress()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(patientMapper.toDTO(patient));
    }
}
```

### Step 5: Create MapStruct Mappers

Create mappers for DTO conversion:

```java
package com.hpms.mapper;

import Model.Patient;
import com.hpms.dto.PatientDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PatientMapper {
    
    @Mapping(source = "createdAt", target = "createdAt", dateFormat = "yyyy-MM-dd'T'HH:mm:ss")
    PatientDTO toDTO(Patient patient);
    
    Patient toEntity(PatientDTO dto);
}
```

## Migration Strategy

### Option 1: Gradual Migration (Recommended)

1. Keep existing Swing UI functional
2. Add Spring Boot REST API alongside
3. Migrate one service at a time
4. Test thoroughly after each migration
5. Eventually deprecate Swing UI if desired

### Option 2: Complete Rewrite

1. Create all JPA entities from models
2. Create all Spring Data repositories
3. Convert all services to Spring beans
4. Wire everything together
5. Replace Swing UI with web frontend (React, Angular, Vue)

## Testing Integration

```java
@SpringBootTest
@AutoConfigureMockMvc
public class PatientControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private PatientService patientService;
    
    @Test
    public void testCreatePatient() throws Exception {
        PatientDTO dto = new PatientDTO();
        dto.setFirstName("John");
        dto.setLastName("Doe");
        // ... set other fields
        
        mockMvc.perform(post("/api/patients")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.firstName").value("John"));
    }
}
```

## Package Structure

After integration, the recommended package structure:

```
src/main/java/
├── com.hpms/
│   ├── Application.java
│   ├── config/          # Spring configuration
│   ├── controller/      # REST controllers
│   ├── dto/            # Data Transfer Objects
│   ├── mapper/         # MapStruct mappers
│   ├── repository/     # JPA repositories
│   ├── security/       # Security components
│   └── exception/      # Exception handling
└── Model/              # Domain entities (add JPA annotations)
└── Service/            # Business logic (add @Service)
└── Util/              # Utilities
```

## Common Issues and Solutions

### Issue: "Table not found"
**Solution**: Ensure JPA entities are properly annotated and `spring.jpa.hibernate.ddl-auto` is set to `update` or `create`.

### Issue: "No qualifying bean"
**Solution**: Make sure services are annotated with `@Service` and repositories with `@Repository`.

### Issue: "Circular dependency"
**Solution**: Use `@Lazy` annotation or refactor services to break circular dependencies.

### Issue: "Transaction not active"
**Solution**: Add `@Transactional` to service methods that perform database operations.

## Production Considerations

1. **Database Migration**: Use Flyway or Liquibase for schema management
2. **Connection Pooling**: Configure HikariCP settings for production load
3. **Caching**: Add Spring Cache for frequently accessed data
4. **Monitoring**: Integrate Spring Actuator for health checks and metrics
5. **Security**: Disable H2 console, use production database, secure JWT secret

## Next Steps

1. Start with Patient entity and service as a proof of concept
2. Create integration tests
3. Migrate remaining entities (Appointment, Billing, etc.)
4. Add business logic validation
5. Implement audit logging
6. Create web frontend or mobile app

## Resources

- [Spring Data JPA Documentation](https://docs.spring.io/spring-data/jpa/docs/current/reference/html/)
- [MapStruct Documentation](https://mapstruct.org/)
- [Spring Security Documentation](https://docs.spring.io/spring-security/reference/)
