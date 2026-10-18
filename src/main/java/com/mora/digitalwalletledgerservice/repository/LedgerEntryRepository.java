package com.mora.digitalwalletledgerservice.repository;

import com.mora.digitalwalletledgerservice.domain.LedgerEntry;
import com.mora.digitalwalletledgerservice.domain.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID> {

    @Query("""
            select coalesce(
                sum(case
                    when e.entryType = :credit then e.amount
                    else -e.amount
                end),
                0
            )
            from LedgerEntry e
            where e.wallet.id = :walletId
            """)
    BigDecimal calculateBalance(@Param("walletId") UUID walletId, @Param("credit") LedgerEntryType credit);

    List<LedgerEntry> findByWalletIdOrderByCreatedAtDesc(UUID walletId);

    List<LedgerEntry> findByTransactionId(UUID transactionId);


}
