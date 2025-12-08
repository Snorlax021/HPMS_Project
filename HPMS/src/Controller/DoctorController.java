package Controller;

import Model.Doctor;
import Service.DoctorService;

public class DoctorController {
    private DoctorService doctorService;

    public DoctorController() { this.doctorService = null; }

    public DoctorController(DoctorService service) {
        this.doctorService = service;
    }

    public void registerDoctor(Doctor doctor) {
        if (doctorService == null) throw new IllegalStateException("DoctorService not initialized");
        doctorService.save(doctor);
    }

    public Doctor getDoctorById(int id) {
        if (doctorService == null) throw new IllegalStateException("DoctorService not initialized");
        return doctorService.findById(id);
    }

    public void updateDoctor(Doctor doctor) {
        if (doctorService == null) throw new IllegalStateException("DoctorService not initialized");
        doctorService.update(doctor);
    }

    public void deleteDoctor(int id) {
        if (doctorService == null) throw new IllegalStateException("DoctorService not initialized");
        doctorService.delete(id);
    }
}
