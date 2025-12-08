package Model;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Patient base profile containing personal medical-related identity.
 * User handles login/credentials; Patient references User and stores health identity info.
 */
public class Patient {
    // Primary identifiers
    private final String patientId; // previous id renamed conceptually
    private final User user; // FK → User (direct reference)
    private final String patientNumber; // MRN auto-generated

    // Demographics and contacts
    private final LocalDate dateOfBirth;
    private final String sex;
    private final String bloodType;
    private final String civilStatus;
    private final String address;
    private final String contactNumber;
    private final String emergencyContactName;
    private final String emergencyContactNumber;

    private final Instant createdAt;

    // Minimal constructor
    public Patient(User user,
                   String patientNumber,
                   LocalDate dateOfBirth,
                   String sex,
                   String bloodType,
                   String civilStatus,
                   String address,
                   String contactNumber,
                   String emergencyContactName,
                   String emergencyContactNumber) {
        this.patientId = UUID.randomUUID().toString();
        this.user = Objects.requireNonNull(user);
        this.patientNumber = requireNonBlank(patientNumber, "patientNumber");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.sex = normalize(sex);
        this.bloodType = normalize(bloodType);
        this.civilStatus = normalize(civilStatus);
        this.address = normalize(address);
        this.contactNumber = normalize(contactNumber);
        this.emergencyContactName = normalize(emergencyContactName);
        this.emergencyContactNumber = normalize(emergencyContactNumber);
        this.createdAt = Instant.now();
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String requireNonBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " must be provided");
        return s.trim();
    }

    public String getPatientId() { return patientId; }
    public User getUser() { return user; }
    public String getUserId() { return user.getId(); }
    public String getPatientNumber() { return patientNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getSex() { return sex; }
    public String getBloodType() { return bloodType; }
    public String getCivilStatus() { return civilStatus; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactNumber() { return emergencyContactNumber; }

    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", userId='" + user.getId() + '\'' +
                ", patientNumber='" + patientNumber + '\'' +
                ", dateOfBirth=" + dateOfBirth +
                ", sex='" + sex + '\'' +
                ", bloodType='" + bloodType + '\'' +
                ", civilStatus='" + civilStatus + '\'' +
                ", address='" + address + '\'' +
                ", contactNumber='" + contactNumber + '\'' +
                ", emergencyContactName='" + emergencyContactName + '\'' +
                ", emergencyContactNumber='" + emergencyContactNumber + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient p = (Patient) o;
        return patientId.equals(p.patientId);
    }

    @Override
    public int hashCode() { return Objects.hash(patientId); }
}