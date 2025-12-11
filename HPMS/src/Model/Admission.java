package Model;

import java.time.LocalDateTime;

public class Admission {
    private String id;
    private String patientId;
    private LocalDateTime admittedAt;
    private String admittedBy;  // userId
    private String wardId;
    private String roomId;
    private String bedId;
    private String admissionReason;
    private AdmissionStatus status;
    private LocalDateTime dischargedAt;
    private String dischargeSummaryId; // link to summary/note

    public Admission() {}

    public Admission(String id,
                     String patientId,
                     LocalDateTime admittedAt,
                     String admittedBy,
                     String wardId,
                     String roomId,
                     String bedId,
                     String admissionReason,
                     AdmissionStatus status,
                     LocalDateTime dischargedAt,
                     String dischargeSummaryId) {
        this.id = id;
        this.patientId = patientId;
        this.admittedAt = admittedAt;
        this.admittedBy = admittedBy;
        this.wardId = wardId;
        this.roomId = roomId;
        this.bedId = bedId;
        this.admissionReason = admissionReason;
        this.status = status;
        this.dischargedAt = dischargedAt;
        this.dischargeSummaryId = dischargeSummaryId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public LocalDateTime getAdmittedAt() { return admittedAt; }
    public void setAdmittedAt(LocalDateTime admittedAt) { this.admittedAt = admittedAt; }

    public String getAdmittedBy() { return admittedBy; }
    public void setAdmittedBy(String admittedBy) { this.admittedBy = admittedBy; }

    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public String getAdmissionReason() { return admissionReason; }
    public void setAdmissionReason(String admissionReason) { this.admissionReason = admissionReason; }

    public AdmissionStatus getStatus() { return status; }
    public void setStatus(AdmissionStatus status) { this.status = status; }

    public LocalDateTime getDischargedAt() { return dischargedAt; }
    public void setDischargedAt(LocalDateTime dischargedAt) { this.dischargedAt = dischargedAt; }

    public String getDischargeSummaryId() { return dischargeSummaryId; }
    public void setDischargeSummaryId(String dischargeSummaryId) { this.dischargeSummaryId = dischargeSummaryId; }
}