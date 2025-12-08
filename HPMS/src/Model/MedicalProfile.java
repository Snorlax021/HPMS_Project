package Model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Medical Profile captures health background (not clinical records).
 */
public class MedicalProfile {
    private final String profileId;
    private final Patient patient; // FK → Patient (direct reference)

    private String knownAllergies;
    private String chronicConditions;
    private String pastSurgeries;
    private String familyMedicalHistory;
    private String immunizationRecord;
    private String lifestyleInfo; // smoking, alcohol, occupation, etc.

    private final Instant createdAt;
    private Instant updatedAt;

    public MedicalProfile(Patient patient) {
        this.profileId = UUID.randomUUID().toString();
        this.patient = Objects.requireNonNull(patient);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    public MedicalProfile(Patient patient,
                          String knownAllergies,
                          String chronicConditions,
                          String pastSurgeries,
                          String familyMedicalHistory,
                          String immunizationRecord,
                          String lifestyleInfo) {
        this.profileId = UUID.randomUUID().toString();
        this.patient = Objects.requireNonNull(patient);
        this.knownAllergies = normalize(knownAllergies);
        this.chronicConditions = normalize(chronicConditions);
        this.pastSurgeries = normalize(pastSurgeries);
        this.familyMedicalHistory = normalize(familyMedicalHistory);
        this.immunizationRecord = normalize(immunizationRecord);
        this.lifestyleInfo = normalize(lifestyleInfo);
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private void touch() { this.updatedAt = Instant.now(); }

    public String getProfileId() { return profileId; }
    public Patient getPatient() { return patient; }
    public String getPatientId() { return patient.getPatientId(); }

    public String getKnownAllergies() { return knownAllergies; }
    public void setKnownAllergies(String knownAllergies) { this.knownAllergies = normalize(knownAllergies); touch(); }

    public String getChronicConditions() { return chronicConditions; }
    public void setChronicConditions(String chronicConditions) { this.chronicConditions = normalize(chronicConditions); touch(); }

    public String getPastSurgeries() { return pastSurgeries; }
    public void setPastSurgeries(String pastSurgeries) { this.pastSurgeries = normalize(pastSurgeries); touch(); }

    public String getFamilyMedicalHistory() { return familyMedicalHistory; }
    public void setFamilyMedicalHistory(String familyMedicalHistory) { this.familyMedicalHistory = normalize(familyMedicalHistory); touch(); }

    public String getImmunizationRecord() { return immunizationRecord; }
    public void setImmunizationRecord(String immunizationRecord) { this.immunizationRecord = normalize(immunizationRecord); touch(); }

    public String getLifestyleInfo() { return lifestyleInfo; }
    public void setLifestyleInfo(String lifestyleInfo) { this.lifestyleInfo = normalize(lifestyleInfo); touch(); }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MedicalProfile)) return false;
        MedicalProfile that = (MedicalProfile) o;
        return profileId.equals(that.profileId);
    }

    @Override
    public int hashCode() { return profileId.hashCode(); }

    @Override
    public String toString() {
        return "MedicalProfile{" +
                "profileId='" + profileId + '\'' +
                ", patientId='" + patient.getPatientId() + '\'' +
                ", knownAllergies='" + knownAllergies + '\'' +
                ", chronicConditions='" + chronicConditions + '\'' +
                ", pastSurgeries='" + pastSurgeries + '\'' +
                ", familyMedicalHistory='" + familyMedicalHistory + '\'' +
                ", immunizationRecord='" + immunizationRecord + '\'' +
                ", lifestyleInfo='" + lifestyleInfo + '\'' +
                '}';
    }
}
