package Controller;

import Model.Patient;
import Service.PatientService;

public class PatientController {
    private final PatientService patientService;

    public PatientController(PatientService service) {
        this.patientService = service;
    }

    public java.util.Optional<Patient> getPatientById(String id) {
        return patientService.findById(id);
    }

    public java.util.Collection<Patient> listAllPatients() {
        return patientService.listAll();
    }

    public boolean deletePatient(String id) {
        return patientService.deletePatient(id);
    }
}