package com.mora.digitalwalletledgerservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mora.digitalwalletledgerservice.domain.*;
import com.mora.digitalwalletledgerservice.dto.TransferRequest;
import com.mora.digitalwalletledgerservice.dto.TransferResponse;
import com.mora.digitalwalletledgerservice.exception.ResourceConflictException;
import com.mora.digitalwalletledgerservice.exception.ResourceNotFoundException;
import com.mora.digitalwalletledgerservice.repository.IdempotencyRecordRepository;
import com.mora.digitalwalletledgerservice.repository.LedgerEntryRepository;
import com.mora.digitalwalletledgerservice.repository.LedgerTransactionRepository;
import com.mora.digitalwalletledgerservice.repository.WalletRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.UUID;

@Service
public class TransferService {

    private final WalletRepository walletRepository;
    private final LedgerTransactionRepository transactionRepository;
    private final LedgerEntryRepository entryRepository;
    private final IdempotencyRecordRepository idempotencyRepository;
    private final ObjectMapper objectMapper;
    private final EntityManager entityManager;

    public TransferService(WalletRepository walletRepository, LedgerTransactionRepository transactionRepository,
                           LedgerEntryRepository entryRepository, IdempotencyRecordRepository idempotencyRepository,
                           ObjectMapper objectMapper, EntityManager entityManager) {

        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.entryRepository = entryRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.objectMapper = objectMapper;
        this.entityManager = entityManager;
    }

    @Transactional
    public TransferResponse transfer(UUID authenticatedUserId, TransferRequest request, String idempotencyKey) {

        validateRequest(authenticatedUserId, request, idempotencyKey);

        String currency = request.currency().trim().toUpperCase(Locale.ROOT);

        String requestHash = calculateRequestHash(request, currency);

        String lockKey = authenticatedUserId + ":" + idempotencyKey;

        entityManager.createNativeQuery("""
                SELECT pg_advisory_xact_lock(
                    hashtextextended(CAST(:lockKey AS text), 0)
                )
                """).setParameter("lockKey", lockKey).getSingleResult();

        var existing = idempotencyRepository.findByUserIdAndIdempotencyKey(authenticatedUserId, idempotencyKey);

        if (existing.isPresent()) {
            var record = existing.get();

            if (!record.getRequestHash().equals(requestHash)) {
                throw new ResourceConflictException("Idempotency key was already used for a different request");
            }

            try {
                return objectMapper.readValue(record.getResponseJson(), TransferResponse.class);
            } catch (JsonProcessingException e) {
                throw new IllegalStateException("Could not deserialize stored transfer response", e);
            }
        }

        UUID sourceId = request.sourceWalletId();
        UUID destinationId = request.destinationWalletId();
        BigDecimal amount = request.amount();


        UUID firstId = sourceId.compareTo(destinationId) < 0 ? sourceId : destinationId;

        UUID secondId = sourceId.compareTo(destinationId) < 0 ? destinationId : sourceId;

        Wallet first = walletRepository.findByIdForUpdate(firstId).orElseThrow(
                () -> new ResourceNotFoundException("Wallet not found: " + firstId));

        Wallet second = walletRepository.findByIdForUpdate(secondId).orElseThrow(
                () -> new ResourceNotFoundException("Wallet not found: " + secondId));

        Wallet source = sourceId.equals(firstId) ? first : second;
        Wallet destination =
                destinationId.equals(firstId) ? first : second;


        if (!source.getUser().getId().equals(authenticatedUserId)) {
            throw new AccessDeniedException("You do not own the source wallet");
        }

        if (source.getStatus() != WalletStatus.ACTIVE || destination.getStatus() != WalletStatus.ACTIVE) {
            throw new ResourceConflictException("Both wallets must be active");
        }

        if (!source.getCurrency().equalsIgnoreCase(currency) || !destination.getCurrency().equalsIgnoreCase(currency)) {
            throw new ResourceConflictException("Currency must match both wallets");
        }

        BigDecimal balance = entryRepository.calculateBalance(sourceId, LedgerEntryType.CREDIT);

        if (balance.compareTo(amount) < 0) {
            throw new ResourceConflictException("Insufficient wallet balance");
        }


        LedgerTransaction transaction = transactionRepository.saveAndFlush(new LedgerTransaction(LedgerTransactionType.TRANSFER));

        entryRepository.save(new LedgerEntry(transaction, source, LedgerEntryType.DEBIT, amount));

        entryRepository.save(new LedgerEntry(transaction, destination, LedgerEntryType.CREDIT, amount));

        transaction.markCompleted();

        entryRepository.flush();

        TransferResponse response = new TransferResponse(transaction.getId(), transaction.getReference(),
                sourceId, destinationId, amount, currency, transaction.getStatus().name(), transaction.getCreatedAt());

        String responseJson;

        try {
            responseJson = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize transfer response", e);
        }


        idempotencyRepository.saveAndFlush(new IdempotencyRecord(authenticatedUserId, idempotencyKey, requestHash, responseJson));

        return response;
    }

    private void validateRequest(UUID authenticatedUserId, TransferRequest request, String idempotencyKey) {

        if (authenticatedUserId == null)
            throw new AccessDeniedException("Authenticated user is required");


        if (request == null || request.sourceWalletId() == null || request.destinationWalletId() == null
                || request.amount() == null || request.currency() == null || request.currency().isBlank()) {
            throw new IllegalArgumentException("All transfer fields are required");
        }

        if (request.sourceWalletId().equals(request.destinationWalletId())) {
            throw new IllegalArgumentException("Source and destination wallets must be different");
        }

        if (request.amount().signum() <= 0 || request.amount().scale() > 4) {
            throw new IllegalArgumentException("Amount must be positive and have at most 4 decimal places");
        }

        if (request.currency().trim().length() != 3) {
            throw new IllegalArgumentException("Currency must be a 3-letter code");
        }

        if (idempotencyKey == null || idempotencyKey.isBlank() || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException("A valid Idempotency-Key is required (1-100 characters)");
        }
    }

    private String calculateRequestHash(TransferRequest request, String currency) {

        String canonicalRequest = String.join("|", request.sourceWalletId().toString(), request.destinationWalletId().toString(),
                request.amount().stripTrailingZeros().toPlainString(), currency);

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(canonicalRequest.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is unavailable", e);
        }
    }
}