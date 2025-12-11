package Model;

import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single work experience entry for a doctor (e.g., hospital employment).
 */
public class DoctorExperience {
    private final String experienceId;
    private final Doctor doctor; // reference to the doctor profile

    private String hospitalName;
    private String position;
    private Integer startYear;
    private Integer endYear; // optional (may be null if ongoing)

    public DoctorExperience(Doctor doctor,
                            String hospitalName,
                            String position,
                            Integer startYear,
                            Integer endYear) {
        this.experienceId = UUID.randomUUID().toString();
        this.doctor = Objects.requireNonNull(doctor);
        this.hospitalName = normalize(hospitalName);
        this.position = normalize(position);
        this.startYear = normalizeYear(startYear);
        this.endYear = normalizeYear(endYear);
        validateYearRange();
    }

    private String normalize(String s) { return (s == null || s.isBlank()) ? null : s.trim(); }

    private Integer normalizeYear(Integer y) {
        if (y == null) return null;
        if (y < 1900) return null; // basic sanity check
        return y;
    }

    private void validateYearRange() {
        if (startYear != null && endYear != null && endYear < startYear) {
            throw new IllegalArgumentException("endYear cannot be before startYear");
        }
    }

    public String getExperienceId() { return experienceId; }
    public Doctor getDoctor() { return doctor; }
    public String getDoctorId() { return doctor.getDoctorId(); }

    public String getHospitalName() { return hospitalName; }
    public void setHospitalName(String hospitalName) { this.hospitalName = normalize(hospitalName); }

    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = normalize(position); }

    public Integer getStartYear() { return startYear; }
    public void setStartYear(Integer startYear) {
        this.startYear = normalizeYear(startYear);
        validateYearRange();
    }

    public Integer getEndYear() { return endYear; }
    public void setEndYear(Integer endYear) {
        this.endYear = normalizeYear(endYear);
        validateYearRange();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorExperience)) return false;
        DoctorExperience that = (DoctorExperience) o;
        return experienceId.equals(that.experienceId);
    }

    @Override
    public int hashCode() { return experienceId.hashCode(); }

    @Override
    public String toString() {
        return "DoctorExperience{" +
                "experienceId='" + experienceId + '\'' +
                ", doctorId='" + doctor.getDoctorId() + '\'' +
                ", hospitalName='" + hospitalName + '\'' +
                ", position='" + position + '\'' +
                ", startYear=" + startYear +
                ", endYear=" + endYear +
                '}';
    }
}
