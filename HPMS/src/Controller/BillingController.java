package Controller;

import Model.Billing;
import Service.BillingService;

public class BillingController {
    private BillingService billingService;

    public BillingController() {
        this.billingService = null;
    }

    public BillingController(BillingService service) {
        this.billingService = service;
    }

    public Billing createInvoice(String patientId, java.math.BigDecimal amount, String description) {
        if (billingService == null) throw new IllegalStateException("BillingService not initialized");
        return billingService.createInvoice(patientId, amount, description);
    }

    public java.util.Optional<Billing> getInvoiceById(String id) {
        if (billingService == null) throw new IllegalStateException("BillingService not initialized");
        return billingService.findById(id);
    }

    public Billing markInvoicePaid(String id) {
        if (billingService == null) throw new IllegalStateException("BillingService not initialized");
        return billingService.markPaid(id);
    }
}
