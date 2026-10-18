
package com.mora.digitalwalletledgerservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "wallets",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wallet_user_currency",
                columnNames = {"user_id", "currency"}
        )
)
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WalletStatus status = WalletStatus.ACTIVE;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Wallet() {}

    public Wallet(AppUser user, String currency) {
        this.user = user;
        this.currency = currency;
    }

    public UUID getId() { return id; }
    public AppUser getUser() { return user; }
    public String getCurrency() { return currency; }
    public WalletStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }

    public void setStatus(WalletStatus status) {
        this.status = status;
    }
}
