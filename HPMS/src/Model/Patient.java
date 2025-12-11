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
    private LocalDate dateOfBirth;
    private String sex;
    private String bloodType;
    private String civilStatus;
    private String address;
    private String contactNumber;
    private String emergencyContactName;
    private String emergencyContactNumber;

    private final Instant createdAt;

    // Additional backward-compatible fields used by UI code
    private String firstName;
    private String lastName;
    private String gender; // compatibility alias for sex

    // Soft-delete flag: archived patients are hidden from active lists
    private boolean archived = false;

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
        this.patientNumber = (patientNumber == null || patientNumber.isBlank()) ? generatePatientNumber() : requireNonBlank(patientNumber, "patientNumber");
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.sex = normalize(sex);
        this.bloodType = normalize(bloodType);
        this.civilStatus = normalize(civilStatus);
        this.address = normalize(address);
        this.contactNumber = normalize(contactNumber);
        this.emergencyContactName = normalize(emergencyContactName);
        this.emergencyContactNumber = normalize(emergencyContactNumber);
        this.createdAt = Instant.now();
        this.firstName = null;
        this.lastName = null;
        this.gender = null;
        this.archived = false;
    }

    // Convenience constructor used by older UI/service code (firstName, lastName, dob, gender, phone, email, address)
    public Patient(String firstName, String lastName, LocalDate dateOfBirth, String gender, String contactNumber, String email, String address) {
        this.patientId = UUID.randomUUID().toString();
        this.user = null;
        this.patientNumber = generatePatientNumber();
        this.dateOfBirth = Objects.requireNonNull(dateOfBirth);
        this.sex = normalize(gender);
        this.firstName = normalize(firstName);
        this.lastName = normalize(lastName);
        this.gender = this.sex;
        this.bloodType = null;
        this.civilStatus = null;
        this.address = normalize(address);
        this.contactNumber = normalize(contactNumber);
        this.emergencyContactName = null;
        this.emergencyContactNumber = null;
        this.createdAt = Instant.now();
        this.archived = false;
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private String requireNonBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " must be provided");
        return s.trim();
    }

    // Public generator for patient number: PT-ID + 2 uppercase letters + 3..10 digits
    public static String generatePatientNumber() {
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        sb.append("PT-ID");
        for (int i = 0; i < 2; i++) sb.append((char) ('A' + rnd.nextInt(26)));
        int digits = 3 + rnd.nextInt(8); // 3..10
        for (int i = 0; i < digits; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    // Backwards-compatible accessors expected by UI code
    public String getId() { return patientId; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getGender() { return gender == null ? sex : gender; }
    // Keep existing getters as well
    public String getPatientId() { return patientId; }
    public User getUser() { return user; }
    public String getUserId() { return user == null ? null : user.getId(); }
    public String getPatientNumber() { return patientNumber; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getSex() { return sex; }
    public String getBloodType() { return bloodType; }
    public String getCivilStatus() { return civilStatus; }
    public String getAddress() { return address; }
    public String getContactNumber() { return contactNumber; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public String getEmergencyContactNumber() { return emergencyContactNumber; }

    // Archived flag accessors
    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public Instant getCreatedAt() { return createdAt; }

    // Setters to allow admin/UI to update patient information in-place
    public void setFirstName(String firstName) { this.firstName = normalize(firstName); }
    public void setLastName(String lastName) { this.lastName = normalize(lastName); }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = Objects.requireNonNull(dateOfBirth); }
    public void setGender(String gender) { this.sex = normalize(gender); this.gender = this.sex; }
    public void setAddress(String address) { this.address = normalize(address); }
    public void setContactNumber(String contactNumber) { this.contactNumber = normalize(contactNumber); }
    public void setEmergencyContactName(String name) { this.emergencyContactName = normalize(name); }
    public void setEmergencyContactNumber(String num) { this.emergencyContactNumber = normalize(num); }
    public void setBloodType(String bt) { this.bloodType = normalize(bt); }
    public void setCivilStatus(String cs) { this.civilStatus = normalize(cs); }

    @Override
    public String toString() {
        return "Patient{" +
                "patientId='" + patientId + '\'' +
                ", userId='" + (user==null?"null":user.getId()) + '\'' +
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
    public int hashCode() { return java.util.Objects.hash(patientId); }
}