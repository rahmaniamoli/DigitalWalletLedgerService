
package com.mora.digitalwalletledgerservice.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransferResponse(
        UUID transactionId,
        UUID reference,
        UUID sourceWalletId,
        UUID destinationWalletId,
        BigDecimal amount,
        String currency,
        String status,
        Instant createdAt
) {}
