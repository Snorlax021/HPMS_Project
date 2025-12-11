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

    // Singleton holder for UI code expecting AppointmentService.getInstance()
    private static class Holder { static final AppointmentService INSTANCE = new AppointmentService(); }
    public static AppointmentService getInstance() { return Holder.INSTANCE; }

    public AppointmentService() {
        this.repo = new InMemoryRepository<>(Appointment::getId);
    }

    public Appointment schedule(String patientId, String staffId, LocalDateTime when, String reason) {
        if (when.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Cannot schedule in the past.");
        }
        Appointment a = new Appointment(patientId, staffId, when.toLocalDate(), when.toLocalTime(), reason);
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

        // Notify patient and staff (if usernames can be resolved)
        try {
            // Resolve patient username (provisioned account or linked user)
            String patientUsername = null;
            try {
                patientUsername = PatientService.getInstance().getProvisionedAccountForPatient(a.getPatientId()).map(pa -> pa.username).orElse(null);
            } catch (Exception ignored) {}
            try {
                if (patientUsername == null) {
                    java.util.Optional<Model.Patient> pp = PatientService.getInstance().findById(a.getPatientId());
                    if (pp.isPresent() && pp.get().getUser() != null) patientUsername = pp.get().getUser().getUsername();
                }
            } catch (Exception ignored) {}

            String doctorUsername = null;
            try {
                java.util.Optional<Model.Doctor> dd = Service.DoctorServiceImpl.getInstance().findByDoctorId(a.getDoctorId());
                if (dd.isPresent() && dd.get().getUser() != null) doctorUsername = dd.get().getUser().getUsername();
            } catch (Exception ignored) {}

            // Notify the staff who requested (if registered)
            try {
                java.util.Optional<String> requester = Service.AppointmentRequestRegistry.getInstance().getRequester(a.getId());
                requester.ifPresent(req -> {
                    NotificationService.getInstance().notifyUser(req, "Appointment " + a.getId() + " has been cancelled.");
                });
            } catch (Exception ignored) {}

            String when = a.getScheduledAt().toString();
            String msg = "Appointment " + a.getId() + " scheduled on " + when + " was cancelled.";
            if (patientUsername != null) NotificationService.getInstance().notifyUser(patientUsername, msg);
            if (doctorUsername != null) NotificationService.getInstance().notifyUser(doctorUsername, msg);
        } catch (Exception ignored) {}

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

    /** Mark an appointment as approved (e.g. doctor accepted request). */
    public Appointment approve(String appointmentId) {
        Optional<Appointment> opt = repo.findById(appointmentId);
        if (opt.isEmpty()) throw new IllegalArgumentException("Appointment not found: " + appointmentId);
        Appointment a = opt.get();
        a.setStatus(AppointmentStatus.APPROVED);
        repo.save(a);

        // Optional: notify patient that appointment was approved
        try {
            String patientUsername = null;
            try { patientUsername = PatientService.getInstance().getProvisionedAccountForPatient(a.getPatientId()).map(pa -> pa.username).orElse(null); } catch (Exception ignored) {}
            try { if (patientUsername == null) { java.util.Optional<Model.Patient> pp = PatientService.getInstance().findById(a.getPatientId()); if (pp.isPresent() && pp.get().getUser() != null) patientUsername = pp.get().getUser().getUsername(); } } catch (Exception ignored) {}
            String when = a.getScheduledAt().toString();
            String msg = "Appointment " + a.getId() + " scheduled on " + when + " was approved by the doctor.";
            if (patientUsername != null) NotificationService.getInstance().notifyUser(patientUsername, msg);
            // Also notify the requesting staff if present
            try { java.util.Optional<String> requester = Service.AppointmentRequestRegistry.getInstance().getRequester(a.getId()); requester.ifPresent(req -> NotificationService.getInstance().notifyUser(req, "Appointment " + a.getId() + " has been approved by the doctor.")); } catch (Exception ignored) {}
        } catch (Exception ignored) {}

        return a;
    }
}