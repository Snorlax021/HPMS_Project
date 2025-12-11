package Model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents an individual time slot in a doctor's schedule.
 */
public class DoctorSchedule {
    private final String scheduleId;
    private final Doctor doctor; // reference to the owning doctor

    private DayOfWeek dayOfWeek;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private boolean available;

    public DoctorSchedule(Doctor doctor, DayOfWeek dayOfWeek, LocalTime timeStart, LocalTime timeEnd, boolean available) {
        this.scheduleId = UUID.randomUUID().toString();
        this.doctor = Objects.requireNonNull(doctor);
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
        this.timeStart = Objects.requireNonNull(timeStart);
        this.timeEnd = Objects.requireNonNull(timeEnd);
        this.available = available;
        validateChronology();
    }

    private void validateChronology() {
        if (!timeEnd.isAfter(timeStart)) {
            throw new IllegalArgumentException("time_end must be after time_start");
        }
    }

    public String getScheduleId() { return scheduleId; }
    public Doctor getDoctor() { return doctor; }
    public String getDoctorId() { return doctor.getDoctorId(); }

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public void setDayOfWeek(DayOfWeek dayOfWeek) { this.dayOfWeek = Objects.requireNonNull(dayOfWeek); }

    public LocalTime getTimeStart() { return timeStart; }
    public void setTimeStart(LocalTime timeStart) {
        this.timeStart = Objects.requireNonNull(timeStart);
        validateChronology();
    }

    public LocalTime getTimeEnd() { return timeEnd; }
    public void setTimeEnd(LocalTime timeEnd) {
        this.timeEnd = Objects.requireNonNull(timeEnd);
        validateChronology();
    }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DoctorSchedule)) return false;
        DoctorSchedule that = (DoctorSchedule) o;
        return scheduleId.equals(that.scheduleId);
    }

    @Override
    public int hashCode() { return scheduleId.hashCode(); }

    @Override
    public String toString() {
        return "DoctorSchedule{" +
                "scheduleId='" + scheduleId + '\'' +
                ", doctorId='" + doctor.getDoctorId() + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                ", available=" + available +
                '}';
    }
}
