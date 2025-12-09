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
import java.util.concurrent.atomic.AtomicInteger;

/** Patient service with basic CRUD plus per-username profile storage. */
public class PatientService {
    private final Repository<String, Patient> repo;
    // Simple runtime cache mapping usernames to profile data
    private final ConcurrentHashMap<String, PatientProfile> profilesByUsername = new ConcurrentHashMap<>();
    // Store the latest provisioned credentials by patientId (transient, for display/testing only)
    private final ConcurrentHashMap<String, ProvisionedAccount> provisionedAccounts = new ConcurrentHashMap<>();
    // Sequence for PT/PW account generation (in-memory). Starts at 1 -> PT0001/PW0001
    private final AtomicInteger patientAccountSeq = new AtomicInteger(0);
    // Singleton holder
    private static final class Holder { static final PatientService INSTANCE = new PatientService(); }
    public static PatientService getInstance() { return Holder.INSTANCE; }

    public PatientService() { this.repo = new InMemoryRepository<>(Patient::getId); initPatientAccountSeqFromUsers(); }

    private void initPatientAccountSeqFromUsers() {
        int max = 0;
        try {
            for (Model.User u : UserService.getInstance().getAllUsers()) {
                String un = u.getUsername();
                if (un != null && un.startsWith("PT") && un.length() >= 6) {
                    String digits = un.substring(2);
                    try { int n = Integer.parseInt(digits); if (n > max) max = n; } catch (NumberFormatException ignored) {}
                }
            }
        } catch (Exception ignored) {}
        patientAccountSeq.set(max);
    }

    public Patient createPatient(String firstName, String lastName, LocalDate dob,
                                 String gender, String phone, String email, String address) {
        Patient p = new Patient(firstName, lastName, dob, gender, phone, email, address);
        Patient saved = repo.save(p);
        // Auto-provision a user account for this patient using PT/PW scheme
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
        // Expanded set of profile fields to match AdminDashboardPanel usage
        public String surname = "";
        public String firstName = "";
        public String middleName = "";
        public String dateOfBirth = "";
        public String gender = "";
        // legacy single-field name used by older code
        public String name = "";
        public String nationality = "";
        public String civilStatus = "";
        public String age = "";

        public String phone = "";
        public String email = "";
        public String address = "";
        public String doctor = "";
        public String emergencyContactName = "";
        public String emergencyContactNumber = "";
        public String emergencyContactRelationship = "";

        public String idType = "";
        public String idNumber = "";
        public String idFrontPath = "";
        public String idBackPath = "";
        public String twoByTwoPath = "";

        public String bloodType = "";
        public String allergies = "";
        public String currentMedications = "";
        public String existingConditions = "";
        public String pastSurgeries = "";
        public String familyMedicalHistory = "";
        public String immunizationHistory = "";
        public String primaryCarePhysician = "";

        public String insuranceProvider = "";
        public String insuranceNumber = "";
        public String insuranceExpiry = "";
        public String philHealthNumber = "";

        public String dateRegistered = "";
        public String patientRecordId = "";
        public String status = "";
        public String assignedDoctor = "";

        public String occupation = "";
        public String employerName = "";
        public String workAddress = "";
        public String religion = "";
        public String preferredLanguage = "";
        public String preferredContactMethod = "";

        // NEW: Additional medical info fields for Patient Dashboard profile
        public String symptoms = "";       // free-form description of current symptoms
        public Double heightCm = null;      // height in centimeters
        public Double weightKg = null;      // weight in kilograms
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

    /**
     * Create a Patient record linked to an existing User without provisioning a new User account.
     * This is used when a patient account is already created (e.g. admin added a user) and
     * we want to store the clinical Patient domain object.
     */
    public Patient createPatientForUser(Model.User user, String firstName, String lastName, LocalDate dob, String gender, String phone, String address) {
        if (user == null) throw new IllegalArgumentException("user required");
        String patientNumber = Model.Patient.generatePatientNumber();
        Model.Patient p = new Model.Patient(user, patientNumber, dob == null ? LocalDate.now().minusYears(20) : dob, gender, null, null, address, phone, null, null);
        repo.save(p);
        // link back to user
        try { user.setLinkedPatientId(p.getId()); } catch (Exception ignored) {}
        return p;
    }
    // --- Internal helpers -------------------------------------------
    private void autoProvisionPatientAccount(Patient patient) {
        if (patient == null) return;
        UserService userService = UserService.getInstance();
        // Generate PT/PW formatted credentials (PT0001, PW0001 ...)
        int seq = patientAccountSeq.incrementAndGet();
        String username = String.format("PT%04d", seq);
        String tempPassword = String.format("PW%04d", seq);
        try {
            userService.createUser(username, tempPassword.toCharArray(), Role.PATIENT);
            userService.findByUsername(username).ifPresent(u -> { try { u.setLinkedPatientId(patient.getId()); } catch (Exception ignored) {} });
            provisionedAccounts.put(patient.getId(), new ProvisionedAccount(username, tempPassword));
        } catch (RuntimeException ex) {
            // If failure due to duplicate or policy, advance and retry once
            try {
                seq = patientAccountSeq.incrementAndGet();
                username = String.format("PT%04d", seq);
                tempPassword = String.format("PW%04d", seq);
                userService.createUser(username, tempPassword.toCharArray(), Role.PATIENT);
                userService.findByUsername(username).ifPresent(u -> { try { u.setLinkedPatientId(patient.getId()); } catch (Exception ignored) {} });
                provisionedAccounts.put(patient.getId(), new ProvisionedAccount(username, tempPassword));
            } catch (RuntimeException ex2) {
                provisionedAccounts.remove(patient.getId());
            }
        }
    }
}