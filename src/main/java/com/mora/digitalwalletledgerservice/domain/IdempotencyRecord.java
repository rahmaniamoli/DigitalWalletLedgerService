
package com.mora.digitalwalletledgerservice.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(
        name = "idempotency_records",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_idempotency_user_key",
                columnNames = {"user_id", "idempotency_key"}
        )
)
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "response_json", nullable = false, columnDefinition = "TEXT")
    private String responseJson;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected IdempotencyRecord() {}

    public IdempotencyRecord(
            UUID userId,
            String idempotencyKey,
            String requestHash,
            String responseJson) {
        this.userId = userId;
        this.idempotencyKey = idempotencyKey;
        this.requestHash = requestHash;
        this.responseJson = responseJson;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public String getResponseJson() {
        return responseJson;
    }
}
