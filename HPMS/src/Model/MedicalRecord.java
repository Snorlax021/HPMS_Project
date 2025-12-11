package Model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single medical record entry for a patient consultation.
 */
public class MedicalRecord {
    private final String recordId;
    private final Patient patient; // FK → Patient (direct reference)
    private final Doctor doctor;   // FK → Doctor (direct reference)

    private String diagnosis;
    private String symptoms;
    private String treatment;
    private String prescriptionSummary;
    private String notes;
    private LocalDate dateOfConsultation;

    public MedicalRecord(Patient patient,
                         Doctor doctor,
                         String diagnosis,
                         String symptoms,
                         String treatment,
                         String prescriptionSummary,
                         String notes,
                         LocalDate dateOfConsultation) {
        this.recordId = UUID.randomUUID().toString();
        this.patient = Objects.requireNonNull(patient);
        this.doctor = Objects.requireNonNull(doctor);
        this.diagnosis = normalize(diagnosis);
        this.symptoms = normalize(symptoms);
        this.treatment = normalize(treatment);
        this.prescriptionSummary = normalize(prescriptionSummary);
        this.notes = normalize(notes);
        this.dateOfConsultation = Objects.requireNonNull(dateOfConsultation);
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    public String getRecordId() { return recordId; }
    public Patient getPatient() { return patient; }
    public String getPatientId() { return patient.getPatientId(); }
    public Doctor getDoctor() { return doctor; }
    public String getDoctorId() { return doctor.getDoctorId(); }

    public String getDiagnosis() { return diagnosis; }
    public void setDiagnosis(String diagnosis) { this.diagnosis = normalize(diagnosis); }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = normalize(symptoms); }

    public String getTreatment() { return treatment; }
    public void setTreatment(String treatment) { this.treatment = normalize(treatment); }

    public String getPrescriptionSummary() { return prescriptionSummary; }
    public void setPrescriptionSummary(String prescriptionSummary) { this.prescriptionSummary = normalize(prescriptionSummary); }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = normalize(notes); }

    public LocalDate getDateOfConsultation() { return dateOfConsultation; }
    public void setDateOfConsultation(LocalDate dateOfConsultation) { this.dateOfConsultation = Objects.requireNonNull(dateOfConsultation); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalRecord)) return false;
        MedicalRecord that = (MedicalRecord) o;
        return recordId.equals(that.recordId);
    }

    @Override
    public int hashCode() { return recordId.hashCode(); }

    @Override
    public String toString() {
        return "MedicalRecord{" +
                "recordId='" + recordId + '\'' +
                ", patientId='" + patient.getPatientId() + '\'' +
                ", doctorId='" + doctor.getDoctorId() + '\'' +
                ", diagnosis='" + diagnosis + '\'' +
                ", symptoms='" + symptoms + '\'' +
                ", treatment='" + treatment + '\'' +
                ", prescriptionSummary='" + prescriptionSummary + '\'' +
                ", notes='" + notes + '\'' +
                ", dateOfConsultation=" + dateOfConsultation +
                '}';
    }
}
