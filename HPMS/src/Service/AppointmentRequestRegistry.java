package Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Optional;

/**
 * Lightweight registry mapping appointmentId -> requester username (staff who created the request).
 * Used so the system can notify the requesting staff when an appointment is cancelled/approved.
 */
public class AppointmentRequestRegistry {
    private final ConcurrentMap<String, String> map = new ConcurrentHashMap<>();
    private static final class Holder { static final AppointmentRequestRegistry INSTANCE = new AppointmentRequestRegistry(); }
    public static AppointmentRequestRegistry getInstance() { return Holder.INSTANCE; }
    private AppointmentRequestRegistry() {}

    public void registerRequest(String appointmentId, String requesterUsername) {
        if (appointmentId == null || appointmentId.isBlank() || requesterUsername == null || requesterUsername.isBlank()) return;
        map.put(appointmentId, requesterUsername.trim().toLowerCase());
    }

    public Optional<String> getRequester(String appointmentId) {
        if (appointmentId == null || appointmentId.isBlank()) return Optional.empty();
        return Optional.ofNullable(map.get(appointmentId));
    }

    public void remove(String appointmentId) { if (appointmentId == null) return; map.remove(appointmentId); }
}
