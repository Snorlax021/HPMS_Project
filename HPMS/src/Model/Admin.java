package Model;

import java.util.Objects;
import java.util.UUID;

public class Admin {
    private final String adminId;
    private final User user; // direct reference to User model
    private String positionOrTitle;
    private String contactNumber;

    // Minimal constructor: provide User, optional position/title and contact number later
    public Admin(User user) {
        this.adminId = UUID.randomUUID().toString();
        this.user = Objects.requireNonNull(user);
    }

    public Admin(User user, String positionOrTitle, String contactNumber) {
        this.adminId = UUID.randomUUID().toString();
        this.user = Objects.requireNonNull(user);
        this.positionOrTitle = normalize(positionOrTitle);
        this.contactNumber = normalize(contactNumber);
    }

    private String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    public String getAdminId() { return adminId; }
    public User getUser() { return user; }
    public String getUserId() { return user.getId(); }
    public String getPositionOrTitle() { return positionOrTitle; }
    public String getContactNumber() { return contactNumber; }

    public void setPositionOrTitle(String positionOrTitle) { this.positionOrTitle = normalize(positionOrTitle); }
    public void setContactNumber(String contactNumber) { this.contactNumber = normalize(contactNumber); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Admin)) return false;
        Admin admin = (Admin) o;
        return adminId.equals(admin.adminId);
    }

    @Override
    public int hashCode() { return Objects.hash(adminId); }

    @Override
    public String toString() {
        return "Admin{" +
                "adminId='" + adminId + '\'' +
                ", userId='" + user.getId() + '\'' +
                ", positionOrTitle='" + positionOrTitle + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                '}';
    }
}