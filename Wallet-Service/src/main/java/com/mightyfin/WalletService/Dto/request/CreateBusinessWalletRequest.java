package com.mightyfin.WalletService.Dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class CreateBusinessWalletRequest {


    @NotNull (message = "businessPublicUuid is required")
    private UUID businessPublicUuid;

    @NotBlank(message = "walletTypeCode is required")
    @Size(max = 64)
    private String walletTypeCode;

    @NotBlank(message = "currencyCode is required")
    @Pattern (
        regexp = "^[A-Za-z]{3}$",
        message = "currencyCode must be a 3-letter ISO 4217 code"
    )
    private String currencyCode;

    public CreateBusinessWalletRequest() {
    }

    public CreateBusinessWalletRequest(UUID businessPublicUuid,String walletTypeCode,String currencyCode) {
        this.businessPublicUuid = businessPublicUuid;
        this.walletTypeCode = walletTypeCode;
        this.currencyCode = currencyCode;
    }
}
