package com.mightyfin.WalletService.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data 
public class CreateCustomerWalletRequest {

    @NotNull (message = "userId is required")
    @Positive(message = "userId must be positive")
    private Integer userId;

    @NotBlank(message = "currencyCode is required")
    @Pattern (
        regexp = "^[A-Za-z]{3}$",
        message = "currencyCode must be a 3-letter ISO 4217 code"
    )
    private String currencyCode;

    public CreateCustomerWalletRequest() {
    }

    public CreateCustomerWalletRequest(Integer userId, String currencyCode) {
        this.userId = userId;
        this.currencyCode = currencyCode;
    }
}
