package DTO;

import java.time.LocalDate;

public class CreatePatientRequest {
    private String fullName;
    private String gender; // or enum name
    private LocalDate dateOfBirth;
    private ContactInfoDTO contactInfo;
    private String insurancePolicyId;

    public CreatePatientRequest() {}

    public CreatePatientRequest(String fullName, String gender, LocalDate dateOfBirth, ContactInfoDTO contactInfo, String insurancePolicyId) {
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = contactInfo;
        this.insurancePolicyId = insurancePolicyId;
    }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public void setDateOfBirth(LocalDate dateOfBirth) { this.dateOfBirth = dateOfBirth; }

    public ContactInfoDTO getContactInfo() { return contactInfo; }
    public void setContactInfo(ContactInfoDTO contactInfo) { this.contactInfo = contactInfo; }

    public String getInsurancePolicyId() { return insurancePolicyId; }
    public void setInsurancePolicyId(String insurancePolicyId) { this.insurancePolicyId = insurancePolicyId; }
}