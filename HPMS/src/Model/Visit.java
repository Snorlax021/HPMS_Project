package Model;

import java.time.LocalDateTime;

public class Visit {
    private String id;
    private String patientId;
    private String doctorId;
    private String appointmentId; // optional link to scheduled appointment
    private LocalDateTime visitDateTime;

    private String reason;    // chief complaint or visit reason
    private String diagnosis; // final diagnosis summary
    private String notes;     // clinician notes

    public Visit() {}

    public Visit(String id,
                 String patientId,
                 String doctorId,
                 String appointmentId,
                 LocalDateTime visitDateTime,
                 String reason,
                 String diagnosis,
                 String notes) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.visitDateTime = visitDateTime;
        this.reason = reason;
        this.diagnosis = diagnosis;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getDoctorId() { return doctorId; }
    public void setDoctorId(String doctorId) { this.doctorId = doctorId; }

    public String getAppointmentId() { return appointmentId; }
    public void setAppointmentId(String appointmentId) { this.appointmentId = appointmentId; }

    public LocalDateTime getVisitDateTime() { return visitDateTime; }
    public void setVisitDateTime(LocalDateTime visitDateTime) { this.visitDateTime = visitDateTime; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = diagnosis; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}