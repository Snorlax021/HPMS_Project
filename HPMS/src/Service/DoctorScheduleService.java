package Service;

import Model.DoctorSchedule;
import Repository.InMemoryRepository;
import Repository.Repository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DoctorScheduleService {
    private final Repository<String, DoctorSchedule> repo;

    private static final class Holder { static final DoctorScheduleService INSTANCE = new DoctorScheduleService(); }
    public static DoctorScheduleService getInstance() { return Holder.INSTANCE; }

    public DoctorScheduleService() {
        this.repo = new InMemoryRepository<>(DoctorSchedule::getScheduleId);
    }

    public DoctorSchedule save(DoctorSchedule s) { return repo.save(s); }
    public Optional<DoctorSchedule> findById(String id) { return repo.findById(id); }
    public Collection<DoctorSchedule> listAll() { return repo.findAll(); }
    public List<DoctorSchedule> listByDoctorId(String doctorId) {
        return repo.findAll().stream().filter(s -> s.getDoctorId()!=null && s.getDoctorId().equals(doctorId)).collect(Collectors.toList());
    }

    /** Convenience: add a new availability slot for a doctor by id */
    public DoctorSchedule addSlot(String doctorId, DayOfWeek day, LocalTime start, LocalTime end) {
        if (doctorId == null || day == null || start == null || end == null) throw new IllegalArgumentException("All fields required");
        // Find doctor by id via DoctorServiceImpl
        Model.Doctor doctor = Service.DoctorServiceImpl.getInstance().findByDoctorId(doctorId).orElseThrow(() -> new IllegalArgumentException("Doctor not found: " + doctorId));
        DoctorSchedule s = new DoctorSchedule(doctor, day, start, end, true);
        return repo.save(s);
    }
    public boolean delete(String id) { return repo.delete(id); }
}