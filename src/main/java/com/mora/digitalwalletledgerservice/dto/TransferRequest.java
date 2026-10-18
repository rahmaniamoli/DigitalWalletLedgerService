package com.mora.digitalwalletledgerservice.dto;



import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record TransferRequest(@NotNull UUID sourceWalletId, @NotNull UUID destinationWalletId,
                              @NotNull @DecimalMin(value = "0.0001") BigDecimal amount, @NotNull @Size(min = 3, max = 3) String currency) {}
