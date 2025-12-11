package Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class LabOrder {
    private String id;
    private String visitId;
    private String patientId;
    private String orderedBy; // userId
    private LocalDateTime orderedAt;
    private LabOrderStatus status;
    private List<String> testsRequested; // test names or codes
    private String notes;

    public LabOrder() {
        this.testsRequested = new ArrayList<>();
    }

    public LabOrder(String id, String visitId, String patientId, String orderedBy, LocalDateTime orderedAt, LabOrderStatus status, List<String> testsRequested, String notes) {
        this.id = id;
        this.visitId = visitId;
        this.patientId = patientId;
        this.orderedBy = orderedBy;
        this.orderedAt = orderedAt;
        this.status = status;
        this.testsRequested = testsRequested == null ? new ArrayList<>() : testsRequested;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getVisitId() { return visitId; }
    public void setVisitId(String visitId) { this.visitId = visitId; }

    public String getPatientId() { return patientId; }
    public void setPatientId(String patientId) { this.patientId = patientId; }

    public String getOrderedBy() { return orderedBy; }
    public void setOrderedBy(String orderedBy) { this.orderedBy = orderedBy; }

    public LocalDateTime getOrderedAt() { return orderedAt; }
    public void setOrderedAt(LocalDateTime orderedAt) { this.orderedAt = orderedAt; }

    public LabOrderStatus getStatus() { return status; }
    public void setStatus(LabOrderStatus status) { this.status = status; }

    public List<String> getTestsRequested() { return testsRequested; }
    public void setTestsRequested(List<String> testsRequested) { this.testsRequested = testsRequested; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}