package Model;

import java.time.LocalDate;

public class Doctor {
    private String id;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;

    // Contact details aligned with Patient
    private ContactInfo contactInfo;

    // Doctor-specific fields
    private String departmentId;   // link to Department
    private String specialization; // e.g., Cardiology, Pediatrics
    private String licenseNumber;  // professional license

    public Doctor() {}

    public Doctor(String id,
                  String fullName,
                  Gender gender,
                  LocalDate dateOfBirth,
                  ContactInfo contactInfo,
                  String departmentId,
                  String specialization,
                  String licenseNumber) {
        this.id = id;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = contactInfo;
        this.departmentId = departmentId;
        this.specialization = specialization;
        this.licenseNumber = licenseNumber;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public ContactInfo getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfo contactInfo) { this.contactInfo = contactInfo; }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = departmentId; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = licenseNumber; }
}