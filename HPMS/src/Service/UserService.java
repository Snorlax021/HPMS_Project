package Service;

import Model.Role;
import Model.User;
import Model.UserStatus;
import Util.PasswordHasher;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * In-memory User service / repository with basic user management and authentication logic.
 *
 * Replace with a DB-backed repo later. This keeps UI code clean and centralizes auth logic.
 */
public class UserService {
    private final Map<String, User> usersByUsername = new ConcurrentHashMap<>();
    // simple in-memory counter for auto-generated passwords (PW000001, PW000002, ...)
    private final AtomicInteger autoPwCounter = new AtomicInteger(1);

    // Singleton holder for sharing the same in-memory store app-wide
    private static class Holder { static final UserService INSTANCE = new UserService(); }
    public static UserService getInstance() { return Holder.INSTANCE; }

    public UserService() {}

    /**
     * Generate the next auto password in plain text (e.g. PW000001) and advance the counter.
     * Caller is responsible for using/clearing this value appropriately.
     */
    public String generateNextPlainPassword() {
        int n = autoPwCounter.getAndIncrement();
        return String.format("PW%06d", n);
    }

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
        // If creating a staff account, assign a staffNumber using ST-ID format
        if (role == Role.STAFF) {
            String sn = generateStaffNumber();
            try { user.setStaffNumber(sn); } catch (Exception ignored) {}
        }
        usersByUsername.put(normalized, user);
        return user;
    }

    // Generate a staff number: ST-ID + 2 uppercase letters + 3..10 digits
    private static String generateStaffNumber() {
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        sb.append("ST-ID");
        for (int i = 0; i < 2; i++) sb.append((char) ('A' + rnd.nextInt(26)));
        int digits = 3 + rnd.nextInt(8);
        for (int i = 0; i < digits; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    public java.util.Optional<User> findByStaffNumber(String staffNumber) {
        if (staffNumber == null) return java.util.Optional.empty();
        String s = staffNumber.trim();
        return usersByUsername.values().stream().filter(u -> u.getStaffNumber() != null && u.getStaffNumber().equalsIgnoreCase(s)).findFirst();
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
        if (!ok) return Optional.empty();
        // Ensure account is active
        if (user.getStatus() != UserStatus.ACTIVE) return Optional.empty();
        return Optional.of(user);
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

    public List<User> getAllUsers() {
        return new ArrayList<>(usersByUsername.values());
    }

    public Optional<User> findById(String id) {
        if (id == null) return Optional.empty();
        return usersByUsername.values().stream().filter(u -> id.equals(u.getId())).findFirst();
    }

    public boolean updateRoleById(String id, Role newRole) {
        Optional<User> opt = findById(id);
        if (opt.isEmpty()) return false;
        opt.get().setRole(newRole);
        return true;
    }

    public boolean resetPasswordById(String id, char[] newPassword) {
        Optional<User> opt = findById(id);
        if (opt.isEmpty()) return false;
        validatePassword(newPassword);
        String newHash = PasswordHasher.hash(newPassword);
        Arrays.fill(newPassword, '\0');
        opt.get().setPasswordHash(newHash);
        return true;
    }

    public boolean deleteById(String id) {
        if (id == null) return false;
        String keyToRemove = null;
        for (Map.Entry<String, User> e : usersByUsername.entrySet()) {
            if (id.equals(e.getValue().getId())) { keyToRemove = e.getKey(); break; }
        }
        if (keyToRemove != null) {
            usersByUsername.remove(keyToRemove);
            return true;
        }
        return false;
    }

    // Deactivate user (soft delete) and activate back
    public boolean deactivateById(String id) {
        Optional<User> opt = findById(id);
        if (opt.isEmpty()) return false;
        User u = opt.get();
        if (u.getRole() == Role.ADMIN) return false; // protect admin
        u.setStatus(Model.UserStatus.INACTIVE);
        return true;
    }

    public boolean activateById(String id) {
        Optional<User> opt = findById(id);
        if (opt.isEmpty()) return false;
        User u = opt.get();
        u.setStatus(Model.UserStatus.ACTIVE);
        return true;
    }

    public java.util.List<User> findDeactivatedByRole(Role role) {
        java.util.List<User> out = new java.util.ArrayList<>();
        for (User u : usersByUsername.values()) if (u.getRole()==role && u.getStatus()!=null && u.getStatus()!=Model.UserStatus.ACTIVE) out.add(u);
        return out;
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
        // Additional demo accounts
        try { createUser("drjohn", "Doctor123".toCharArray(), Role.DOCTOR); } catch (Exception ignored) {}
        try { createUser("staffjane", "Staff1234".toCharArray(), Role.STAFF); } catch (Exception ignored) {}
        // New patient login account: fred / Fred1234
        try { createUser("fred", "Fred1234".toCharArray(), Role.PATIENT); } catch (Exception ignored) {}
    }
}