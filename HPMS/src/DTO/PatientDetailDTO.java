package DTO;

import java.time.LocalDate;
import java.util.List;

public class PatientDetailDTO {
    private String id;
    private String fullName;
    private String gender;
    private LocalDate dateOfBirth;
    private ContactInfoDTO contactInfo;
    private String insurancePolicyId;
    private List<String> allergySummaries; // simple strings for UI
    private List<String> currentAdmissionIds; // optional list of active admissions

    public PatientDetailDTO() {}

    public PatientDetailDTO(String id, String fullName, String gender, LocalDate dateOfBirth, ContactInfoDTO contactInfo, String insurancePolicyId, List<String> allergySummaries, List<String> currentAdmissionIds) {
        this.id = id;
        this.fullName = fullName;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.contactInfo = contactInfo;
        this.insurancePolicyId = insurancePolicyId;
        this.allergySummaries = allergySummaries;
        this.currentAdmissionIds = currentAdmissionIds;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public List<String> getAllergySummaries() { return allergySummaries; }
    public void setAllergySummaries(List<String> allergySummaries) { this.allergySummaries = allergySummaries; }

    public List<String> getCurrentAdmissionIds() { return currentAdmissionIds; }
    public void setCurrentAdmissionIds(List<String> currentAdmissionIds) { this.currentAdmissionIds = currentAdmissionIds; }
}