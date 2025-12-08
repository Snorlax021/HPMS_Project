package Model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single education entry for a doctor (e.g., MBBS, MD, etc.).
 */
public class DoctorEducation {
    private final String educationId;
    private final Doctor doctor; // reference to the doctor profile

    private String schoolName;
    private String degree;
    private Integer yearGraduated; // optional; use Integer to allow nulls

    public DoctorEducation(Doctor doctor, String schoolName, String degree, Integer yearGraduated) {
        this.educationId = UUID.randomUUID().toString();
        this.doctor = Objects.requireNonNull(doctor);
        this.schoolName = normalize(schoolName);
        this.degree = normalize(degree);
        this.yearGraduated = normalizeYear(yearGraduated);
    }

    private String normalize(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private Integer normalizeYear(Integer y) {
        if (y == null) return null;
        if (y < 1900) return null; // basic sanity check
        return y;
    }

    public String getEducationId() { return educationId; }
    public Doctor getDoctor() { return doctor; }
    public String getDoctorId() { return doctor.getDoctorId(); }

    public String getSchoolName() { return schoolName; }
    public void setSchoolName(String schoolName) { this.schoolName = normalize(schoolName); }

    public String getDegree() { return degree; }
    public void setDegree(String degree) { this.degree = normalize(degree); }

    public Integer getYearGraduated() { return yearGraduated; }
    public void setYearGraduated(Integer yearGraduated) { this.yearGraduated = normalizeYear(yearGraduated); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorEducation)) return false;
        DoctorEducation that = (DoctorEducation) o;
        return educationId.equals(that.educationId);
    }

    @Override
    public int hashCode() { return educationId.hashCode(); }

    @Override
    public String toString() {
        return "DoctorEducation{" +
                "educationId='" + educationId + '\'' +
                ", doctorId='" + doctor.getDoctorId() + '\'' +
                ", schoolName='" + schoolName + '\'' +
                ", degree='" + degree + '\'' +
                ", yearGraduated=" + yearGraduated +
                '}';
    }
}
