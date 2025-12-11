package Service;

import java.time.Instant;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Simple in-memory hospital service for announcements/logs created by admins
 * and for editable hospital service descriptions maintaned by admins.
 */
public class HospitalService {
    public static class Announcement {
        public final String id;
        public final String targetUsername; // null == all
        public final String text;
        public final Instant createdAt;
        public final String createdBy; // optional admin username

        public Announcement(String id, String targetUsername, String text, Instant createdAt, String createdBy) {
            this.id = id;
            this.targetUsername = targetUsername;
            this.text = text;
            this.createdAt = createdAt;
            this.createdBy = createdBy;
        }

        @Override
        public String toString() {
            String target = (targetUsername==null?"All Patients":targetUsername);
            return String.format("[%s] (%s) %s", createdAt.toString(), target, text);
        }
    }

    // Simple model for editable hospital services (category descriptions)
    public static class ServiceEntry {
        public final String category;
        private String description;
        private Instant lastUpdated;
        private String updatedBy;
        public ServiceEntry(String category, String description, Instant lastUpdated, String updatedBy) {
            this.category = category;
            this.description = description;
            this.lastUpdated = lastUpdated;
            this.updatedBy = updatedBy;
        }
        public String getDescription() { return description; }
        public Instant getLastUpdated() { return lastUpdated; }
        public String getUpdatedBy() { return updatedBy; }
        public void setDescription(String desc, Instant at, String by) { this.description = desc; this.lastUpdated = at; this.updatedBy = by; }
        @Override public String toString() { return category + " — last updated: " + (lastUpdated==null?"(never)":lastUpdated.toString()) + " by " + (updatedBy==null?"(unknown)":updatedBy); }
    }

    private final ConcurrentMap<String, List<Announcement>> announcementsByUser = new ConcurrentHashMap<>();
    private final List<Announcement> globalAnnouncements = Collections.synchronizedList(new ArrayList<>());

    // service entries keyed by lowercase category
    private final ConcurrentMap<String, ServiceEntry> servicesByCategory = new ConcurrentHashMap<>();

    private static final class Holder { static final HospitalService INSTANCE = new HospitalService(); }
    public static HospitalService getInstance() { return Holder.INSTANCE; }

    private HospitalService() {
        // Seed some default service descriptions so patient UI shows content immediately
        setServiceDescription("Surgery", "Surgery Department\n\nLead Surgeon: Dr. Anthony Rivera\nSpecialties: General surgery, minimally invasive procedures.\nAvailability: Mon-Fri, 7:00 AM - 6:00 PM.\nContact: surgery@hospital.example", "system");
        setServiceDescription("Radiology", "Radiology Department\n\nChief Radiologist: Dr. Sophia Nguyen\nServices: X-Ray, MRI, CT, Ultrasound.\nAvailability: Mon-Sat, 8:00 AM - 8:00 PM.\nContact: radiology@hospital.example", "system");
        setServiceDescription("Pharmacy", "Pharmacy\n\nHead Pharmacist: Mr. Daniel Perez, RPh\nServices: Prescriptions, medication counseling, refills.\nAvailability: Mon-Sun, 9:00 AM - 9:00 PM.\nContact: pharmacy@hospital.example", "system");
        setServiceDescription("Pediatrics", "Pediatrics\n\nAttending Pediatrician: Dr. Emily Carter\nServices: Well-child visits, immunizations, acute care.\nAvailability: Mon-Fri, 9:00 AM - 5:00 PM.\nContact: pediatrics@hospital.example", "system");
        setServiceDescription("Cardiology", "Cardiology\n\nConsultant Cardiologist: Dr. Raj Patel\nServices: ECG, echocardiogram, stress tests, heart health.\nAvailability: Mon-Fri, 8:00 AM - 4:00 PM.\nContact: cardiology@hospital.example", "system");
        setServiceDescription("Orthopedics", "Orthopedics\n\nOrthopedic Surgeon: Dr. Laura Kim\nServices: Bone/joint care, sports injuries, rehabilitation.\nAvailability: Mon-Fri, 10:00 AM - 6:00 PM.\nContact: ortho@hospital.example", "system");
    }

    /** Add an announcement targeting a given username; pass null to target all users. */
    public Announcement addAnnouncement(String targetUsername, String text, String createdBy) {
        String id = java.util.UUID.randomUUID().toString();
        Announcement a = new Announcement(id, targetUsername, text, Instant.now(), createdBy);
        if (targetUsername == null) {
            globalAnnouncements.add(0, a);
        } else {
            announcementsByUser.compute(targetUsername, (k, list) -> {
                if (list == null) list = new ArrayList<>();
                list.add(0, a);
                return list;
            });
        }
        return a;
    }

    /** List announcements visible to the given username (includes global announcements). */
    public List<Announcement> listAnnouncementsForUser(String username) {
        List<Announcement> out = new ArrayList<>();
        synchronized (globalAnnouncements) { out.addAll(globalAnnouncements); }
        List<Announcement> perUser = announcementsByUser.get(username);
        if (perUser != null) out.addAll(perUser);
        return out.stream().collect(Collectors.toList());
    }

    /** List global announcements only. */
    public List<Announcement> listGlobalAnnouncements() { synchronized(globalAnnouncements){ return new ArrayList<>(globalAnnouncements); } }

    /** List per-user announcements (may be empty). */
    public List<Announcement> listAnnouncementsForSpecificUser(String username) { List<Announcement> l = announcementsByUser.get(username); return l==null?new ArrayList<>():new ArrayList<>(l); }

    // ------------------ Service descriptions API ---------------------
    /** Set or update the service description for a category. Category is case-insensitive. */
    public ServiceEntry setServiceDescription(String category, String description, String updatedBy) {
        if (category == null || category.isBlank()) throw new IllegalArgumentException("category required");
        String key = category.trim().toLowerCase();
        ServiceEntry e = servicesByCategory.compute(key, (k, existing) -> {
            if (existing == null) return new ServiceEntry(category.trim(), description == null ? "" : description, Instant.now(), updatedBy);
            existing.setDescription(description == null ? "" : description, Instant.now(), updatedBy);
            return existing;
        });
        return e;
    }

    /** Get the description text for a category, or null if not present. */
    public String getServiceDescription(String category) {
        if (category == null) return null;
        ServiceEntry e = servicesByCategory.get(category.trim().toLowerCase());
        return e == null ? null : e.getDescription();
    }

    /** List all service categories currently known. */
    public List<String> listServiceCategories() { return servicesByCategory.values().stream().map(s -> s.category).collect(Collectors.toList()); }

    /** List all service entries (copy). */
    public List<ServiceEntry> listServiceEntries() { return servicesByCategory.values().stream().collect(Collectors.toList()); }
}