package Model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Doctor {
    private final String doctorId;
    private final User user; // FK → User (direct reference)

    private String specialization;
    private String licenseNumber;
    private LocalDate licenseExpiry;
    private int yearsOfExperience;
    private String departmentId;
    private BigDecimal consultationFee;
    private String biography; // description
    private String contactNumber;
    private UserStatus status; // active/inactive

    public Doctor(User user) {
        this.doctorId = generateDoctorId();
        this.user = Objects.requireNonNull(user);
        this.status = UserStatus.ACTIVE;
    }

    public Doctor(User user,
                  String specialization,
                  String licenseNumber,
                  LocalDate licenseExpiry,
                  int yearsOfExperience,
                  String departmentId,
                  BigDecimal consultationFee,
                  String biography,
                  String contactNumber,
                  UserStatus status) {
        this.doctorId = generateDoctorId();
        this.user = Objects.requireNonNull(user);
        this.specialization = normalize(specialization);
        this.licenseNumber = normalize(licenseNumber);
        this.licenseExpiry = licenseExpiry;
        this.yearsOfExperience = Math.max(0, yearsOfExperience);
        this.departmentId = normalize(departmentId);
        this.consultationFee = normalizeFee(consultationFee);
        this.biography = normalize(biography);
        this.contactNumber = normalize(contactNumber);
        this.status = status == null ? UserStatus.ACTIVE : status;
    }

    // Generate doctor id in the format: DR-XXX-YY (DR- then 3 letters then 2 digits)
    private static String generateDoctorId() {
        java.util.Random rnd = new java.util.Random();
        StringBuilder sb = new StringBuilder();
        sb.append("DR-ID"); // literal prefix required by spec
        // two uppercase letters
        for (int i = 0; i < 2; i++) sb.append((char) ('A' + rnd.nextInt(26)));
        // 3-10 digits
        int digits = 3 + rnd.nextInt(8); // 3..10
        for (int i = 0; i < digits; i++) sb.append(rnd.nextInt(10));
        return sb.toString();
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }
    private BigDecimal normalizeFee(BigDecimal fee) {
        if (fee == null) return null;
        if (fee.signum() < 0) return BigDecimal.ZERO;
        return fee;
    }

    public String getDoctorId() { return doctorId; }
    public User getUser() { return user; }
    public String getUserId() { return user.getId(); }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = normalize(specialization); }

    public String getLicenseNumber() { return licenseNumber; }
    public void setLicenseNumber(String licenseNumber) { this.licenseNumber = normalize(licenseNumber); }

    public LocalDate getLicenseExpiry() { return licenseExpiry; }
    public void setLicenseExpiry(LocalDate licenseExpiry) { this.licenseExpiry = licenseExpiry; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = Math.max(0, yearsOfExperience); }

    public String getDepartmentId() { return departmentId; }
    public void setDepartmentId(String departmentId) { this.departmentId = normalize(departmentId); }

    public BigDecimal getConsultationFee() { return consultationFee; }
    public void setConsultationFee(BigDecimal consultationFee) { this.consultationFee = normalizeFee(consultationFee); }

    public String getBiography() { return biography; }
    public void setBiography(String biography) { this.biography = normalize(biography); }

    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = normalize(contactNumber); }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status == null ? UserStatus.ACTIVE : status; }
}