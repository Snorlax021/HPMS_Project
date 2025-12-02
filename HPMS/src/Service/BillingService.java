package Service;

import Model.Billing;
import Model.BillingStatus;
import Repository.InMemoryRepository;
import Repository.Repository;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Optional;

/** Lightweight billing service to create and update invoices. */
public class BillingService {
    private final Repository<String, Billing> repo;

    public BillingService() {
        this.repo = new InMemoryRepository<>(Billing::getId);
    }

    public Billing createInvoice(String patientId, BigDecimal amount, String description) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount must be non-negative.");
        }
        Billing b = new Billing(patientId, amount, description);
        return repo.save(b);
    }

    public Optional<Billing> findById(String id) { return repo.findById(id); }
    public Collection<Billing> listAll() { return repo.findAll(); }

    public Billing markPaid(String id) {
        Optional<Billing> opt = repo.findById(id);
        if (opt.isEmpty()) throw new IllegalArgumentException("Invoice not found: " + id);
        Billing b = opt.get();
        b.setStatus(BillingStatus.PAID);
        repo.save(b);
        return b;
    }
}