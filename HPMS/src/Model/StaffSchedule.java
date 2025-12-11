package Model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Represents a single scheduled shift for a staff member.
 */
public class StaffSchedule {
    private final String scheduleId;
    private final Staff staff; // reference to the staff profile

    private DayOfWeek dayOfWeek;
    private LocalTime timeStart;
    private LocalTime timeEnd;
    private ShiftType shiftType;
    private boolean available;

    public enum ShiftType {
        MORNING,
        NIGHT,
        SPLIT
    }

    public StaffSchedule(Staff staff,
                         DayOfWeek dayOfWeek,
                         LocalTime timeStart,
                         LocalTime timeEnd,
                         ShiftType shiftType,
                         boolean available) {
        this.scheduleId = UUID.randomUUID().toString();
        this.staff = Objects.requireNonNull(staff);
        this.dayOfWeek = Objects.requireNonNull(dayOfWeek);
        this.timeStart = Objects.requireNonNull(timeStart);
        this.timeEnd = Objects.requireNonNull(timeEnd);
        this.shiftType = Objects.requireNonNull(shiftType);
        this.available = available;
        validateChronology();
    }

    private void validateChronology() {
        if (!timeEnd.isAfter(timeStart)) {
            throw new IllegalArgumentException("time_end must be after time_start");
        }
    }

    public String getScheduleId() { return scheduleId; }
    public Staff getStaff() { return staff; }
    public String getStaffId() { return staff.getStaffId(); }

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

    public ShiftType getShiftType() { return shiftType; }
    public void setShiftType(ShiftType shiftType) { this.shiftType = Objects.requireNonNull(shiftType); }

    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StaffSchedule)) return false;
        StaffSchedule that = (StaffSchedule) o;
        return scheduleId.equals(that.scheduleId);
    }

    @Override
    public int hashCode() { return scheduleId.hashCode(); }

    @Override
    public String toString() {
        return "StaffSchedule{" +
                "scheduleId='" + scheduleId + '\'' +
                ", staffId='" + staff.getStaffId() + '\'' +
                ", dayOfWeek=" + dayOfWeek +
                ", timeStart=" + timeStart +
                ", timeEnd=" + timeEnd +
                ", shiftType=" + shiftType +
                ", available=" + available +
                '}';
    }
}
