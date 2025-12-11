package DTO;

public class UserDTO {
    private String id;
    private String username;
    private String role; // e.g., ADMIN, STAFF, DOCTOR
    private String linkedPatientId;
    private String linkedDoctorId;
    private boolean enabled;

    public UserDTO() {}

    public UserDTO(String id, String username, String role, String linkedPatientId, String linkedDoctorId, boolean enabled) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.linkedPatientId = linkedPatientId;
        this.linkedDoctorId = linkedDoctorId;
        this.enabled = enabled;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getLinkedPatientId() { return linkedPatientId; }
    public void setLinkedPatientId(String linkedPatientId) { this.linkedPatientId = linkedPatientId; }

    public String getLinkedDoctorId() { return linkedDoctorId; }
    public void setLinkedDoctorId(String linkedDoctorId) { this.linkedDoctorId = linkedDoctorId; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}