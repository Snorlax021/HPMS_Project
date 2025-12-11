package DTO;

import java.util.List;

public class RoomWithBedsDTO {
    private String id;
    private String wardId;
    private String roomNumber;
    private int capacity;
    private String notes;
    private List<BedDTO> beds;

    public RoomWithBedsDTO() {}

    public RoomWithBedsDTO(String id, String wardId, String roomNumber, int capacity, String notes, List<BedDTO> beds) {
        this.id = id;
        this.wardId = wardId;
        this.roomNumber = roomNumber;
        this.capacity = capacity;
        this.notes = notes;
        this.beds = beds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getWardId() { return wardId; }
    public void setWardId(String wardId) { this.wardId = wardId; }

    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<BedDTO> getBeds() { return beds; }
    public void setBeds(List<BedDTO> beds) { this.beds = beds; }
}