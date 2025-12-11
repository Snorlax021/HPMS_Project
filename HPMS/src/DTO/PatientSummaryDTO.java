package DTO;

import java.time.LocalDateTime;

public class PatientSummaryDTO {
    private String id;
    private String fullName;
    private Integer age; // computed on server
    private String gender;
    private String status; // e.g., CHECKED_IN / ADMITTED / DISCHARGED
    private String roomId;
    private String bedId;
    private LocalDateTime admittedAt;

    public PatientSummaryDTO() {}

    public PatientSummaryDTO(String id, String fullName, Integer age, String gender, String status, String roomId, String bedId, LocalDateTime admittedAt) {
        this.id = id;
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.status = status;
        this.roomId = roomId;
        this.bedId = bedId;
        this.admittedAt = admittedAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }

    public String getBedId() { return bedId; }
    public void setBedId(String bedId) { this.bedId = bedId; }

    public LocalDateTime getAdmittedAt() { return admittedAt; }
    public void setAdmittedAt(LocalDateTime admittedAt) { this.admittedAt = admittedAt; }
}
