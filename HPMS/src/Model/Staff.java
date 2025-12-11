package Model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Core staff profile model, parent for all staff-related information.
 */
public class Staff {
    private final String staffId;
    private final User user; // FK → User (direct reference)

    private String firstName;
    private String lastName;
    private String roleType; // e.g., Nurse, Clerk, Receptionist, Midwife, Technician
    private String departmentId;
    private String contactNumber;
    private LocalDate hireDate;
    private UserStatus status; // active/inactive

    // Minimal constructor: requires user, optional details set later
    public Staff(User user) {
        this.staffId = generateStaffId();
        this.user = Objects.requireNonNull(user);
        this.status = UserStatus.ACTIVE;
    }

    // Full constructor
    public Staff(User user,
                 String firstName,
                 String lastName,
                 String roleType,
                 String departmentId,
                 String contactNumber,
                 LocalDate hireDate,
                 UserStatus status) {
        this.staffId = generateStaffId();
        this.user = Objects.requireNonNull(user);
        this.firstName = normalize(firstName);
        this.lastName = normalize(lastName);
        this.roleType = normalize(roleType);
        this.departmentId = normalize(departmentId);
        this.contactNumber = normalize(contactNumber);
        this.hireDate = hireDate;
        this.status = status == null ? UserStatus.ACTIVE : status;
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    public String getStaffId() { return staffId; }
    public User getUser() { return user; }
    public String getUserId() { return user.getId(); }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = normalize(firstName); }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = normalize(lastName); }

    public String getRoleType() { return roleType; }
    public void setRoleType(String roleType) { this.roleType = normalize(roleType); }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = normalize(departmentId); }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = normalize(contactNumber); }

    public LocalDate getHireDate() { return hireDate; }
    public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status == null ? UserStatus.ACTIVE : status; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Staff)) return false;
        Staff staff = (Staff) o;
        return staffId.equals(staff.staffId);
    }

    @Override
    public int hashCode() { return staffId.hashCode(); }

    @Override
    public String toString() {
        return "Staff{" +
                "staffId='" + staffId + '\'' +
                ", userId='" + user.getId() + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roleType='" + roleType + '\'' +
                ", departmentId='" + departmentId + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", hireDate=" + hireDate +
                ", status=" + status +
                '}';
    }

    private static String generateStaffId() {
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        sb.append("ST-ID");
        for (int i = 0; i < 2; i++) sb.append((char) ('A' + rnd.nextInt(26)));
        int digits = 3 + rnd.nextInt(8); // 3..10
        for (int i = 0; i < digits; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }
}