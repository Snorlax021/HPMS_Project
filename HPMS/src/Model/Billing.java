package Model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Simple billing/invoice model.
 */
public class Billing {
    private final String id;
    private final String patientId;
    private final BigDecimal amount;
    private final String description;
    private BillingStatus status;
    private final Instant createdAt;

    public Billing(String patientId, BigDecimal amount, String description) {
        this.id = UUID.randomUUID().toString();
        this.patientId = Objects.requireNonNull(patientId);
        this.amount = Objects.requireNonNull(amount);
        this.description = description;
        this.status = BillingStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public String getId() { 
    	return id; 
    	}
    public String getPatientId() { 
    	return patientId; 
    	}
    public BigDecimal getAmount() { 
    	return amount; 
    	}
    public String getDescription() { 
    	return description; 
    	}
    public BillingStatus getStatus() { 
    	return status; 
    	}
    public Instant getCreatedAt() { 
    	return createdAt; 
    	}

    public void setStatus(BillingStatus status) { 
    	this.status = status; 
    	}

    @Override public String toString() { 
    	return "Billing{" + id + " " + amount + "}"; 
    	}
}