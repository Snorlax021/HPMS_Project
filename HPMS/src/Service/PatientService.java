package Service;

import Model.Patient;
import Repository.InMemoryRepository;
import Repository.Repository;
import DTO.PatientSummaryDTO;
import Model.Role;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Random;

/** Patient service with basic CRUD plus per-username profile storage. */
public class PatientService {
    private final Repository<String, Patient> repo;
    // Simple runtime cache mapping usernames to profile data
    private final ConcurrentHashMap<String, PatientProfile> profilesByUsername = new ConcurrentHashMap<>();
    // Store the latest provisioned credentials by patientId (transient, for display/testing only)
    private final ConcurrentHashMap<String, ProvisionedAccount> provisionedAccounts = new ConcurrentHashMap<>();
    // Singleton holder
    private static final class Holder { static final PatientService INSTANCE = new PatientService(); }
    public static PatientService getInstance() { return Holder.INSTANCE; }

    public PatientService() { this.repo = new InMemoryRepository<>(Patient::getId); }

    public Patient createPatient(String firstName, String lastName, LocalDate dob,
                                 String gender, String phone, String email, String address) {
        Patient p = new Patient(firstName, lastName, dob, gender, phone, email, address);
        Patient saved = repo.save(p);
        // Auto-provision a user account for this patient
        autoProvisionPatientAccount(saved);
        return saved;
    }

    public Optional<Patient> findById(String id) { return repo.findById(id); }

    public Collection<Patient> listAll() { return repo.findAll(); }

    public boolean deletePatient(String id) { return repo.delete(id); }

    // --- Profile data per username ---------------------------------
    public PatientProfile getProfileByUsername(String username) {
        return profilesByUsername.computeIfAbsent(username, k -> new PatientProfile());
    }
    public void saveProfile(String username, PatientProfile profile) {
        if (username == null || username.isBlank() || profile == null) return;
        profilesByUsername.put(username, profile);
    }

    /**
     * Return a basic PatientSummaryDTO for the given patient ID, if found.
     * Age is computed from dateOfBirth. Other fields left null when not available.
     */
    public java.util.Optional<PatientSummaryDTO> getPatientSummaryById(String id) {
        if (id == null || id.isBlank()) return java.util.Optional.empty();
        java.util.Optional<Model.Patient> opt = findById(id);
        if (opt.isEmpty()) return java.util.Optional.empty();
        Model.Patient p = opt.get();
        PatientSummaryDTO dto = new PatientSummaryDTO();
        dto.setId(p.getId());
        dto.setFullName(p.getFirstName() + " " + p.getLastName());
        dto.setGender(p.getGender());
        dto.setAge(computeAge(p.getDateOfBirth()));
        // status/room/bed/admittedAt are not tracked here; leave null
        return java.util.Optional.of(dto);
    }

    private Integer computeAge(java.time.LocalDate dob) {
        if (dob == null) return null;
        java.time.Period period = java.time.Period.between(dob, java.time.LocalDate.now());
        return Math.max(0, period.getYears());
    }

    /** Lightweight DTO to hold patient-facing profile fields. */
    public static class PatientProfile {
        public String name = "";
        public String age = "";
        public String bloodType = "";
        public String gender = "";
        public String address = "";
        public String doctor = "";
        public String email = "";
        public String phone = "";
    }

    /** Username + temp password issued to a newly created patient account. */
    public static class ProvisionedAccount {
        public final String username;
        public final String temporaryPassword; // display-only; not stored hashed here
        public ProvisionedAccount(String u, String p) { this.username = u; this.temporaryPassword = p; }
    }

    /** Returns the last generated credentials for the given patientId, if any (for UI display). */
    public Optional<ProvisionedAccount> getProvisionedAccountForPatient(String patientId) {
        return Optional.ofNullable(provisionedAccounts.get(patientId));
    }

    // --- Internal helpers -------------------------------------------
    private void autoProvisionPatientAccount(Patient patient) {
        if (patient == null) return;
        UserService userService = UserService.getInstance();
        String base = (patient.getFirstName() + "." + patient.getLastName()).toLowerCase().replaceAll("[^a-z0-9]+", ".");
        String username = base;
        int attempt = 0;
        while (userService.findByUsername(username).isPresent()) {
            attempt++;
            username = base + attempt;
            if (attempt > 1000) { // fallback to UUID tail
                username = base + Long.toHexString(Double.doubleToLongBits(Math.random())).substring(8);
                break;
            }
        }
        String tempPassword = generateTempPassword(patient);
        try {
            userService.createUser(username, tempPassword.toCharArray(), Role.PATIENT);
            userService.linkUserToPatient(username, patient.getId());
            provisionedAccounts.put(patient.getId(), new ProvisionedAccount(username, tempPassword));
        } catch (RuntimeException ex) {
            // In case password policy or other issues, ensure we don't crash patient creation
            provisionedAccounts.remove(patient.getId());
        }
    }

    private String generateTempPassword(Patient p) {
        // Pattern: Capitalized first name prefix + yyyy of DOB (or 1990 if null) + random 3-digit
        String first = (p.getFirstName() == null || p.getFirstName().isBlank()) ? "Patient" : p.getFirstName().trim();
        String cap = first.substring(0, Math.min(1, first.length())).toUpperCase() + first.substring(Math.min(1, first.length())).toLowerCase();
        String year = (p.getDateOfBirth() != null) ? String.valueOf(p.getDateOfBirth().getYear()) : "1990";
        int rnd = 100 + new Random().nextInt(900);
        String pwd = cap + year + rnd; // contains letters+digits, length >= 8 for most names
        // Ensure policy compliance (>=8 chars, letters+digits). If too short, append more digits.
        while (pwd.length() < 8) pwd += Integer.toString(new Random().nextInt(10));
        return pwd;
    }
}