
package com.mora.digitalwalletledgerservice.controller;

import com.mora.digitalwalletledgerservice.dto.TransferRequest;
import com.mora.digitalwalletledgerservice.dto.TransferResponse;
import com.mora.digitalwalletledgerservice.repository.AppUserRepository;
import com.mora.digitalwalletledgerservice.service.TransferService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/transfers")
public class TransferController {

    private final TransferService transferService;
    private final AppUserRepository appUserRepository;

    public TransferController(TransferService transferService, AppUserRepository appUserRepository) {
        this.transferService = transferService;
        this.appUserRepository = appUserRepository;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request,
                                     @RequestHeader("Idempotency-Key") String idempotencyKey, Authentication authentication) {

        UUID userId = appUserRepository.findByEmail(authentication.getName()).orElseThrow(() ->
                new UsernameNotFoundException("Authenticated user not found")).getId();

        return transferService.transfer(userId, request, idempotencyKey);
    }
}
