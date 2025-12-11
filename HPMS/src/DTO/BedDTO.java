package DTO;

public class BedDTO {
    private String id;
    private String bedNumber;
    private boolean occupied;
    private String currentAdmissionId;

    public BedDTO() {}

    public BedDTO(String id, String bedNumber, boolean occupied, String currentAdmissionId) {
        this.id = id;
        this.bedNumber = bedNumber;
        this.occupied = occupied;
        this.currentAdmissionId = currentAdmissionId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    public String getCurrentAdmissionId() { return currentAdmissionId; }
    public void setCurrentAdmissionId(String currentAdmissionId) { this.currentAdmissionId = currentAdmissionId; }
}