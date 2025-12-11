package Model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Appointment model linking patient and doctor schedule.
 */
public class Appointment {
    private final String id; // appointment_id
    private final String patientId;
    private final String doctorId; // required doctor reference
    private final LocalDate scheduleDate;
    private final LocalTime scheduleTime;
    private final String reason;
    private AppointmentStatus status;
    private final Instant createdAt;

    public Appointment(String patientId, String doctorId, LocalDate scheduleDate, LocalTime scheduleTime, String reason) {
        this.id = UUID.randomUUID().toString();
        this.patientId = Objects.requireNonNull(patientId);
        this.doctorId = Objects.requireNonNull(doctorId);
        this.scheduleDate = Objects.requireNonNull(scheduleDate);
        this.scheduleTime = Objects.requireNonNull(scheduleTime);
        this.reason = reason;
        this.status = AppointmentStatus.PENDING; // default to pending
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getPatientId1() { return patientId; }
    public String getDoctorId() { return doctorId; }
    public LocalDate getScheduleDate() { return scheduleDate; }
    public LocalTime getScheduleTime() { return scheduleTime; }
    public String getReason() { return reason; }
    public AppointmentStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    // Compatibility methods expected by older UI
    public String getPatientId() { return patientId; }
    public String getStaffId() { return doctorId; }
    public java.time.LocalDateTime getScheduledAt() { return java.time.LocalDateTime.of(scheduleDate, scheduleTime); }
    public AppointmentStatus getAppointmentStatus() { return status; }

    public void setStatus(AppointmentStatus status) { this.status = Objects.requireNonNull(status); }

    @Override public String toString() {
        return "Appointment{" + id + " on " + scheduleDate + " at " + scheduleTime + "}";
    }
}