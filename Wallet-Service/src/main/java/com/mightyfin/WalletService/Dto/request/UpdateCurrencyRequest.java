package com.mightyfin.WalletService.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCurrencyRequest(@NotBlank(message = "Name is required") @Size (max = 100)  String name) {

}
