package Model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple immutable-ish user model. Password is stored as a hashed string (see PasswordHasher).
 */
public class User {
    private final String id;
    private final String username;
    private volatile String passwordHash; // mutable through service
    private volatile Role role;
    private final Instant createdAt;

    public User(String username, String passwordHash, Role role) {
        this.id = UUID.randomUUID().toString();
        this.username = Objects.requireNonNull(username).trim();
        this.passwordHash = Objects.requireNonNull(passwordHash);
        this.role = Objects.requireNonNull(role);
        this.createdAt = Instant.now();
    }

    public String getId() { 
        return id;
         }

    public String getUsername() { 
        return username; 
        }

    public String getPasswordHash() { 
        return passwordHash; 
        } // service only - do not expose broadly

    public Role getRole() { 
        return role; 
        }

    public Instant getCreatedAt() { 
        return createdAt; 
        }

    // Package-private setters used by UserService
    public void setPasswordHash(String passwordHash) { 
        this.passwordHash = passwordHash; 
        }

    void setRole(Role role) { 
        this.role = role; 
        }

    @Override
    public String toString() {
        return "User{" +
            "id='" + id + '\'' +
            ", username='" + username + '\'' +
            ", role=" + role +
            ", createdAt=" + createdAt +
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