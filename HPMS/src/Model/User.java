package Model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple immutable-ish user model. Password is stored as a hashed string (see PasswordHasher).
 */
public class User {
    private final String id;
    // Full name of the user
    private volatile String fullName;
    // Distinct username and email (email may be used for login)
    private final String username;
    private volatile String email;
    private volatile String passwordHash; // mutable through service
    private volatile Role role;
    // Optional profile picture URL/path
    private volatile String profilePictureUrl;
    // Active/Inactive status
    private volatile UserStatus status;
    private final Instant createdAt;
    private volatile Instant updatedAt;
    // New: optional linkage to domain entities (patient/doctor). For now, only patient link is used.
    private volatile String linkedPatientId;

    // Back-compat constructor: assumes username doubles as email and full name unknown
    public User(String username, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = Objects.requireNonNull(username).trim();
        this.email = this.username; // assumption: legacy used username/email interchangeably
        this.fullName = this.username; // default placeholder; can be updated later
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    // New constructor with full fields
    public User(String username, String email, String fullName, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = Objects.requireNonNull(username).trim();
        this.email = Objects.requireNonNull(email).trim();
        this.fullName = Objects.requireNonNull(fullName).trim();
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.status = UserStatus.ACTIVE;
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public String getId() { return id; }

    public String getFullName() { return fullName; }

    public String getUsername() { return username; }

    public String getEmail() { return email; }

    public String getPasswordHash() { return passwordHash; } // service only - do not expose broadly

    public Role getRole() { return role; }

    public String getProfilePictureUrl() { return profilePictureUrl; }

    public UserStatus getStatus() { return status; }

    public Instant getCreatedAt() { return createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }

    // New: linked patient accessor
    public String getLinkedPatientId() { return linkedPatientId; }
    public void setLinkedPatientId(String linkedPatientId) {
        this.linkedPatientId = linkedPatientId;
        this.updatedAt = Instant.now();
    }

    // Package-private setters used by UserService
    void setPasswordHash(String passwordHash) {
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.updatedAt = Instant.now();
    }

    void setRole(Role role) {
        this.role = Objects.requireNonNull(role);
        this.updatedAt = Instant.now();
    }

    void setFullName(String fullName) {
        this.fullName = Objects.requireNonNull(fullName).trim();
        this.updatedAt = Instant.now();
    }

    void setEmail(String email) {
        this.email = Objects.requireNonNull(email).trim();
        this.updatedAt = Instant.now();
    }

    void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = (profilePictureUrl == null || profilePictureUrl.isBlank()) ? null : profilePictureUrl.trim();
        this.updatedAt = Instant.now();
    }

    void setStatus(UserStatus status) {
        this.status = Objects.requireNonNull(status);
        this.updatedAt = Instant.now();
    }

    @Override
    public String toString() {
        return "User{" +
            "id='" + id + '\'' +
            ", username='" + username + '\'' +
            ", email='" + email + '\'' +
            ", fullName='" + fullName + '\'' +
            ", role=" + role +
            ", status=" + status +
            ", linkedPatientId=" + linkedPatientId +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return id.equals(user.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}