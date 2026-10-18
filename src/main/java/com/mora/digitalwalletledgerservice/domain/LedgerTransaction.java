
package com.mora.digitalwalletledgerservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "ledger_transactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_ledger_transaction_reference",
                columnNames = "reference"
        )
)
public class LedgerTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID reference = UUID.randomUUID();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LedgerTransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private LedgerTransactionStatus status =
            LedgerTransactionStatus.PENDING;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected LedgerTransaction() {}

    public LedgerTransaction(LedgerTransactionType type) {
        this.type = type;
    }

    public UUID getId() { return id; }
    public UUID getReference() { return reference; }
    public LedgerTransactionType getType() { return type; }
    public LedgerTransactionStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void markCompleted() {
        this.status = LedgerTransactionStatus.COMPLETED;
    }
}
