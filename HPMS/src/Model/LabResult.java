package Model;

import java.time.LocalDateTime;

public class LabResult {
    private String id;
    private String labOrderId;
    private String testName;
    private String resultValue;
    private String units;
    private String normalRange;
    private boolean flagged;
    private String reportedBy; // userId or lab technician id
    private LocalDateTime reportedAt;
    private String notes;

    public LabResult() {}

    public LabResult(String id, String labOrderId, String testName, String resultValue, String units, String normalRange, boolean flagged, String reportedBy, LocalDateTime reportedAt, String notes) {
        this.id = id;
        this.labOrderId = labOrderId;
        this.testName = testName;
        this.resultValue = resultValue;
        this.units = units;
        this.normalRange = normalRange;
        this.flagged = flagged;
        this.reportedBy = reportedBy;
        this.reportedAt = reportedAt;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getLabOrderId() { return labOrderId; }
    public void setLabOrderId(String labOrderId) { this.labOrderId = labOrderId; }

    public String getTestName() { return testName; }
    public void setTestName(String testName) { this.testName = testName; }

    public String getResultValue() { return resultValue; }
    public void setResultValue(String resultValue) { this.resultValue = resultValue; }

    public String getUnits() { return units; }
    public void setUnits(String units) { this.units = units; }

    public String getNormalRange() { return normalRange; }
    public void setNormalRange(String normalRange) { this.normalRange = normalRange; }

    public boolean isFlagged() { return flagged; }
    public void setFlagged(boolean flagged) { this.flagged = flagged; }

    public String getReportedBy() { return reportedBy; }
    public void setReportedBy(String reportedBy) { this.reportedBy = reportedBy; }

    public LocalDateTime getReportedAt() { return reportedAt; }
    public void setReportedAt(LocalDateTime reportedAt) { this.reportedAt = reportedAt; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}