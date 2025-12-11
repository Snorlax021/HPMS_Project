package Controller;

import Model.Appointment;
import Service.AppointmentService;

public class AppointmentController {
    private AppointmentService appointmentService;

    public AppointmentController() { this.appointmentService = null; }

    public AppointmentController(AppointmentService service) {
        this.appointmentService = service;
    }

    public Appointment scheduleAppointment(String patientId, String staffId, java.time.LocalDateTime when, String reason) {
        if (appointmentService == null) throw new IllegalStateException("AppointmentService not initialized");
        return appointmentService.schedule(patientId, staffId, when, reason);
    }

    public java.util.Optional<Appointment> getAppointmentById(String id) {
        if (appointmentService == null) throw new IllegalStateException("AppointmentService not initialized");
        return appointmentService.findById(id);
    }

    public Appointment cancelAppointment(String id) {
        if (appointmentService == null) throw new IllegalStateException("AppointmentService not initialized");
        return appointmentService.cancel(id);
    }
}