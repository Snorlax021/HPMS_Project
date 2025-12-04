package Service;

import Model.Patient;
import Repository.InMemoryRepository;
import Repository.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Patient service with basic CRUD plus per-username profile storage. */
public class PatientService {
    private final Repository<String, Patient> repo;
    // Simple runtime cache mapping usernames to profile data
    private final ConcurrentHashMap<String, PatientProfile> profilesByUsername = new ConcurrentHashMap<>();
    // Singleton holder
    private static final class Holder { static final PatientService INSTANCE = new PatientService(); }
    public static PatientService getInstance() { return Holder.INSTANCE; }

    public PatientService() {
        this.repo = new InMemoryRepository<>(Patient::getId);
    }

    public Patient createPatient(String firstName, String lastName, LocalDate dob,
                                 String gender, String phone, String email, String address) {
        Patient p = new Patient(firstName, lastName, dob, gender, phone, email, address);
        return repo.save(p);
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
}