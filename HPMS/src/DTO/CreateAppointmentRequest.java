package DTO;

import java.time.LocalDateTime;

public class CreateAppointmentRequest {
    private String patientId;
    private String doctorId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String reason;

    public CreateAppointmentRequest() {}

    public CreateAppointmentRequest(String patientId, String doctorId, LocalDateTime startTime, LocalDateTime endTime, String reason) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reason = reason;
    }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}