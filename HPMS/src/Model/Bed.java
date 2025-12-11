package Model;

public class Bed {
    private String id;
    private String roomId;
    private String bedNumber;
    private boolean occupied;
    private String currentAdmissionId; // the Admission.id if occupied
    private String notes;

    public Bed() {}

    public Bed(String id, String roomId, String bedNumber, boolean occupied, String currentAdmissionId, String notes) {
        this.id = id;
        this.roomId = roomId;
        this.bedNumber = bedNumber;
        this.occupied = occupied;
        this.currentAdmissionId = currentAdmissionId;
        this.notes = notes;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getBedNumber() { return bedNumber; }
    public void setBedNumber(String bedNumber) { this.bedNumber = bedNumber; }

    public boolean isOccupied() { return occupied; }
    public void setOccupied(boolean occupied) { this.occupied = occupied; }

    public String getCurrentAdmissionId() { return currentAdmissionId; }
    public void setCurrentAdmissionId(String currentAdmissionId) { this.currentAdmissionId = currentAdmissionId; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}