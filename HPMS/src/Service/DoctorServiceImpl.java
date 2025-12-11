package Service;

import Model.Doctor;
import Repository.InMemoryRepository;
import Repository.Repository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Simple in-memory implementation of DoctorService used by the UI.
 * Provides listAll and basic operations plus soft-deactivate/activate support.
 */
public class DoctorServiceImpl implements DoctorService {
    private final Repository<String, Doctor> repo;

    private static final class Holder { static final DoctorServiceImpl INSTANCE = new DoctorServiceImpl(); }
    public static DoctorServiceImpl getInstance() { return Holder.INSTANCE; }

    public DoctorServiceImpl() { this.repo = new InMemoryRepository<>(Doctor::getDoctorId); }

    @Override
    public void save(Doctor d) { repo.save(d); }

    @Override
    public Doctor findById(int id) {
        // compatibility: Doctor.getDoctorId is a String; this interface used int -> return null
        return null;
    }

    public Optional<Doctor> findByDoctorId(String id) { return repo.findById(id); }

    @Override
    public void update(Doctor d) { repo.save(d); }

    @Override
    public void delete(int id) {
        // intentionally no-op: permanent delete not allowed via service used by UI
        throw new UnsupportedOperationException("Permanent delete not supported");
    }

    public Collection<Doctor> listAll() { return repo.findAll(); }

    public java.util.List<Doctor> listActive() {
        return repo.findAll().stream().filter(d -> d.getStatus()!=null && d.getStatus().name().equals("ACTIVE")).collect(Collectors.toList());
    }

    public boolean deactivateByDoctorId(String doctorId) {
        Optional<Doctor> opt = repo.findById(doctorId);
        if (opt.isEmpty()) return false;
        opt.get().setStatus(Model.UserStatus.INACTIVE);
        repo.save(opt.get());
        return true;
    }

    public boolean activateByDoctorId(String doctorId) {
        Optional<Doctor> opt = repo.findById(doctorId);
        if (opt.isEmpty()) return false;
        opt.get().setStatus(Model.UserStatus.ACTIVE);
        repo.save(opt.get());
        return true;
    }
}
