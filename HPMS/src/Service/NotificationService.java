package Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Lightweight in-memory notification service for demo purposes.
 * Stores brief string notifications per username and allows retrieval + clear.
 */
public class NotificationService {
    private final ConcurrentMap<String, List<String>> byUser = new ConcurrentHashMap<>();

    private static final class Holder { static final NotificationService INSTANCE = new NotificationService(); }
    public static NotificationService getInstance() { return Holder.INSTANCE; }

    private NotificationService() {}

    /** Add a notification for the given username (non-null). */
    public void notifyUser(String username, String message) {
        if (username == null || username.isBlank() || message == null) return;
        byUser.compute(username.trim().toLowerCase(), (k, list) -> {
            if (list == null) list = new ArrayList<>();
            list.add(message);
            return list;
        });
    }

    /** Retrieve and clear notifications for the username. Returns an immutable copy. */
    public List<String> getAndClearNotifications(String username) {
        if (username == null || username.isBlank()) return List.of();
        String k = username.trim().toLowerCase();
        List<String> list = byUser.remove(k);
        if (list == null) return List.of();
        return List.copyOf(list);
    }
}
