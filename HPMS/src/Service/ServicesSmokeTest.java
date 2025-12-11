package Service;

import Model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ServicesSmokeTest {
    public static void main(String[] args) {
        // Patient service
        PatientService ps = new PatientService();
        Patient p = ps.createPatient("Jane", "Doe", LocalDate.of(1990, 1, 1), "F", "1234567890", "jane@example.com", "123 St");
        System.out.println("Created patient id=" + p.getId());
        ps.getProvisionedAccountForPatient(p.getId()).ifPresent(acc -> {
            System.out.println("Auto-provisioned account -> username=" + acc.username + ", tempPassword=" + acc.temporaryPassword);
        });

        // Billing service
        BillingService bs = new BillingService();
        Billing invoice = bs.createInvoice(p.getId(), new BigDecimal("100.50"), "Consultation");
        System.out.println("Invoice status before: " + invoice.getStatus());
        bs.markPaid(invoice.getId());
        System.out.println("Invoice status after:  " + invoice.getStatus());

        // Appointment service
        AppointmentService as = new AppointmentService();
        Appointment appt = as.schedule(p.getId(), "staff-1", LocalDateTime.now().plusMinutes(10), "Checkup");
        System.out.println("Appointment status before: " + appt.getStatus());
        as.cancel(appt.getId());
        System.out.println("Appointment status after cancel: " + appt.getStatus());
        // complete again to ensure transitions update object
        as.complete(appt.getId());
        System.out.println("Appointment status after complete: " + appt.getStatus());
    }
}