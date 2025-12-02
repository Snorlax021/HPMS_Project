package Service;

import Model.Patient;
import Repository.InMemoryRepository;
import Repository.Repository;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;

/** Patient service with basic CRUD. */
public class PatientService {
    private final Repository<String, Patient> repo;

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
}