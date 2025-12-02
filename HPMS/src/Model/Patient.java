package Model;

import java.time.LocalDate;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Patient domain model.
 */
public class Patient {
    private final String id;
    private final String firstName;
    private final String lastName;
    private final LocalDate dateOfBirth;
    private final String gender;
    private final String phone;
    private final String email;
    private final String address;
    private final Instant createdAt;

    public Patient(String firstName, String lastName, LocalDate dateOfBirth,
                   String gender, String phone, String email, String address) {
        this.id = UUID.randomUUID().toString();
        this.firstName = Objects.requireNonNull(firstName).trim();
        this.lastName = Objects.requireNonNull(lastName).trim();
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.createdAt = Instant.now();
    }

    public String getId() { return id; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public LocalDate getDateOfBirth() { return dateOfBirth; }
    public String getGender() { return gender; }
    public String getPhone() { return phone; }
    public String getEmail() { return email; }
    public String getAddress() { return address; }
    public Instant getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Patient{" + id + " " + firstName + " " + lastName + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Patient)) return false;
        Patient p = (Patient) o;
        return id.equals(p.id);
    }

    @Override
    public int hashCode() { return Objects.hash(id); }
}