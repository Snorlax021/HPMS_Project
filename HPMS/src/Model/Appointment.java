package Model;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Appointment model linking patient and staff (optional during scheduling).
 */
public class Appointment {
    private final String id;
    private final String patientId;
    private final String staffId; // may be null if not assigned yet
    private final LocalDateTime scheduledAt;
    private final String reason;
    private AppointmentStatus status;
    private final Instant createdAt;

    public Appointment(String patientId, String staffId, LocalDateTime scheduledAt, String reason) {
        this.id = UUID.randomUUID().toString();
        this.patientId = Objects.requireNonNull(patientId);
        this.staffId = staffId;
        this.scheduledAt = Objects.requireNonNull(scheduledAt);
        this.reason = reason;
        this.status = AppointmentStatus.SCHEDULED;
        this.createdAt = Instant.now();
    }

    public String getId() { 
    	return id; 
    	}
    public String getPatientId() { 
    	return patientId; 
    	}
    public String getStaffId() { 
    	return staffId; 
    	}
    public LocalDateTime getScheduledAt() { 
    	return scheduledAt; 
    	}
    public String getReason() { 
    	return reason;
    	}
    public AppointmentStatus getStatus() { 
    	return status; 
    	}
    public Instant getCreatedAt() { 
    	return createdAt; 
    	}

    public void setStatus(AppointmentStatus status) { 
    	this.status = status; 
    	}

    @Override public String toString() { 
    	return "Appointment{" + id + " at " + scheduledAt + "}"; 
    	}
}