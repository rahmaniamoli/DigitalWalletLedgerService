
package com.mora.digitalwalletledgerservice.dto;

import com.mora.digitalwalletledgerservice.domain.LedgerEntryType;
import com.mora.digitalwalletledgerservice.domain.LedgerTransactionType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TransactionHistoryResponse(
        UUID entryId,
        UUID transactionId,
        UUID reference,
        LedgerTransactionType transactionType,
        LedgerEntryType entryType,
        BigDecimal amount,
        Instant createdAt
) {}
