
package com.mora.digitalwalletledgerservice.controller;

import com.mora.digitalwalletledgerservice.dto.TransactionHistoryResponse;
import com.mora.digitalwalletledgerservice.dto.WalletResponse;
import com.mora.digitalwalletledgerservice.service.WalletService;
import com.mora.digitalwalletledgerservice.repository.AppUserRepository;

import jakarta.validation.constraints.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final WalletService walletService;
    private final AppUserRepository userRepository;

    public WalletController(WalletService walletService, AppUserRepository userRepository) {
        this.walletService = walletService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public WalletResponse createWallet(@RequestParam @Pattern(regexp = "[A-Za-z]{3}") String currency, Authentication authentication) {

        UUID userId = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found")).getId();

        return walletService.createWallet(userId, currency);
    }

    @GetMapping
    public List<WalletResponse> getWallets(Authentication authentication) {

        UUID userId = getUserId(authentication);
        return walletService.getWallets(userId);
    }

    @GetMapping("/{walletId}")
    public WalletResponse getWallet(@PathVariable UUID walletId, Authentication authentication) {

        return walletService.getWallet(getUserId(authentication), walletId);
    }

    @GetMapping("/{walletId}/transactions")
    public List<TransactionHistoryResponse> getTransactionHistory(@PathVariable UUID walletId, Authentication authentication) {

        return walletService.getTransactionHistory(getUserId(authentication), walletId);
    }

    private UUID getUserId(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found"))
                .getId();
    }
}
