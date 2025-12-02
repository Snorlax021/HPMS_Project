package Service;

import Model.Role;
import Model.User;
import Util.PasswordHasher;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays; // added for secure clearing of password char arrays

/**
 * In-memory User service / repository with basic user management and authentication logic.
 *
 * Replace with a DB-backed repo later. This keeps UI code clean and centralizes auth logic.
 */
public class UserService {
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();

    public UserService() {}

    /**
     * Create a new user. Username must be unique (case-insensitive).
     * Password passed as char[] and will be hashed.
     * This method now proactively clears the provided password array after hashing to reduce
     * its lifetime in memory. Callers SHOULD NOT rely on the password array contents after
     * this call. If the caller still needs the original chars, pass a copy instead.
     */
    public User createUser(String username, char[] password, Role role) {
        String normalized = username.trim().toLowerCase();
        if (usersByUsername.containsKey(normalized)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        validatePassword(password);
        String hashed = PasswordHasher.hash(password);
        // Clear password chars ASAP (best-effort)
        Arrays.fill(password, '\0');
        User user = new User(username.trim(), hashed, role);
        usersByUsername.put(normalized, user);
        return user;
    }

    /**
     * Authenticate with username and password. Returns the User if auth succeeded.
     * Does NOT clear the password array so the caller can decide lifecycle; clearing can be
     * added if desired. (We avoid clearing here to prevent surprises for callers that reuse it.)
     */
    public Optional<User> authenticate(String username, char[] password) {
        if (username == null) return Optional.empty();
        String normalized = username.trim().toLowerCase();
        User user = usersByUsername.get(normalized);
        if (user == null) return Optional.empty();
        boolean ok = PasswordHasher.verify(password, user.getPasswordHash());
        return ok ? Optional.of(user) : Optional.empty();
    }

    /**
     * Change password for an existing user. Returns true if changed.
     * Clears both currentPassword and newPassword arrays (best-effort) after use.
     */
    public boolean changePassword(String username, char[] currentPassword, char[] newPassword) {
        Optional<User> opt = authenticate(username, currentPassword);
        boolean authenticated = opt.isPresent();
        // Clear current password regardless of outcome
        if (currentPassword != null) Arrays.fill(currentPassword, '\0');
        if (!authenticated) return false;
        validatePassword(newPassword);
        String newHash = PasswordHasher.hash(newPassword);
        // Clear new password chars ASAP
        Arrays.fill(newPassword, '\0');
        User user = opt.get();
        user.setPasswordHash(newHash);
        return true;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable(usersByUsername.get(username.trim().toLowerCase()));
    }

    // Simple password policy: minimum length and at least one digit and one letter.
    private void validatePassword(char[] password) {
        if (password == null || password.length < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long.");
        }
        boolean hasDigit = false, hasLetter = false;
        for (char c : password) {
            if (Character.isDigit(c)) hasDigit = true;
            if (Character.isLetter(c)) hasLetter = true;
            if (hasDigit && hasLetter) break;
        }
        if (!hasDigit || !hasLetter) {
            throw new IllegalArgumentException("Password must contain at least one letter and one digit.");
        }
    }

    /**
     * Seed demo users matching earlier UI demo accounts (admin/admin123, doctor/doctor123, staff/staff123, patient/patient123).
     * Weak demo passwords are deliberately used for convenience; DO NOT use in production.
     * Call this from app startup in dev/demo builds.
     */
    public void createDefaultDemoUsers() {
        try { createUser("admin", "admin123".toCharArray(), Role.ADMIN); } catch (Exception ignored) {}
        try { createUser("doctor", "doctor123".toCharArray(), Role.DOCTOR); } catch (Exception ignored) {}
        try { createUser("staff", "staff123".toCharArray(), Role.STAFF); } catch (Exception ignored) {}
        try { createUser("patient", "patient123".toCharArray(), Role.PATIENT); } catch (Exception ignored) {}
    }
}