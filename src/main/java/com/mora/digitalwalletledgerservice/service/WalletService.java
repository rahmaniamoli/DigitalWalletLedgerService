
package com.mora.digitalwalletledgerservice.service;

import com.mora.digitalwalletledgerservice.domain.AppUser;
import com.mora.digitalwalletledgerservice.domain.LedgerEntryType;
import com.mora.digitalwalletledgerservice.domain.Wallet;
import com.mora.digitalwalletledgerservice.dto.TransactionHistoryResponse;
import com.mora.digitalwalletledgerservice.dto.WalletResponse;
import com.mora.digitalwalletledgerservice.exception.ResourceConflictException;
import com.mora.digitalwalletledgerservice.exception.ResourceNotFoundException;
import com.mora.digitalwalletledgerservice.repository.AppUserRepository;
import com.mora.digitalwalletledgerservice.repository.LedgerEntryRepository;
import com.mora.digitalwalletledgerservice.repository.WalletRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final AppUserRepository userRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public WalletService(
            WalletRepository walletRepository,
            AppUserRepository userRepository,
            LedgerEntryRepository ledgerEntryRepository) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public WalletResponse createWallet(UUID userId, String currency) {
        if (currency == null || !currency.matches("[A-Za-z]{3}")) {
            throw new IllegalArgumentException("Currency must be a 3-letter code");
        }

        String normalizedCurrency = currency.toUpperCase();

        AppUser user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (walletRepository.findByUserId(userId).stream().anyMatch(w -> w.getCurrency().equals(normalizedCurrency))) {
            throw new ResourceConflictException("User already has a wallet in this currency");
        }

        Wallet wallet = walletRepository.save(new Wallet(user, normalizedCurrency));

        return new WalletResponse(wallet.getId(), wallet.getCurrency(), wallet.getStatus().name(), BigDecimal.ZERO, wallet.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID userId, UUID walletId) {
        Wallet wallet = getOwnedWallet(userId, walletId);

        return new WalletResponse(wallet.getId(), wallet.getCurrency(), wallet.getStatus().name(), getBalance(wallet.getId()), wallet.getCreatedAt());
    }

    @Transactional(readOnly = true)
    public List<WalletResponse> getWallets(UUID userId) {
        return walletRepository.findByUserId(userId).stream()
                .map(wallet -> new WalletResponse(wallet.getId(), wallet.getCurrency(),
                        wallet.getStatus().name(), getBalance(wallet.getId()), wallet.getCreatedAt()))
                .toList();
    }

    private Wallet getOwnedWallet(UUID userId, UUID walletId) {
        return walletRepository.findByIdAndUserId(walletId, userId).orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    }

    private BigDecimal getBalance(UUID walletId) {
        BigDecimal balance = ledgerEntryRepository.calculateBalance(walletId, LedgerEntryType.CREDIT);

        return balance == null ? BigDecimal.ZERO : balance;
    }


    @Transactional(readOnly = true)
    public List<TransactionHistoryResponse> getTransactionHistory(UUID userId, UUID walletId) {
        getOwnedWallet(userId, walletId);

        return ledgerEntryRepository.findByWalletIdOrderByCreatedAtDesc(walletId).stream().map(entry -> new TransactionHistoryResponse(
                        entry.getId(),
                        entry.getTransaction().getId(),
                        entry.getTransaction().getReference(),
                        entry.getTransaction().getType(),
                        entry.getEntryType(),
                        entry.getAmount(),
                        entry.getCreatedAt()
                )).toList();
    }

}
