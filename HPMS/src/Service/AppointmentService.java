package Service;

import Model.Appointment;
import Model.AppointmentStatus;
import Repository.InMemoryRepository;
import Repository.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

/** Appointment scheduling and lifecycle operations. */
public class AppointmentService {
    private final Repository<String, Appointment> repo;

    public AppointmentService() {
        this.repo = new InMemoryRepository<>(Appointment::getId);
    }

    public Appointment schedule(String patientId, String staffId, LocalDateTime when, String reason) {
        if (when.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule in the past.");
        }
        Appointment a = new Appointment(patientId, staffId, when, reason);
        return repo.save(a);
    }

    public Optional<Appointment> findById(String id) { return repo.findById(id); }

    public Collection<Appointment> listAll() { return repo.findAll(); }

    public Appointment cancel(String appointmentId) {
        Optional<Appointment> opt = repo.findById(appointmentId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        Appointment a = opt.get();
        a.setStatus(AppointmentStatus.CANCELLED);
        repo.save(a);
        return a;
    }

    public Appointment complete(String appointmentId) {
        Optional<Appointment> opt = repo.findById(appointmentId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        Appointment a = opt.get();
        a.setStatus(AppointmentStatus.COMPLETED);
        repo.save(a);
        return a;
    }
}