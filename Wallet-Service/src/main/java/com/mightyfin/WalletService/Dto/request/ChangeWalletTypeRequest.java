package com.mightyfin.WalletService.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class ChangeWalletTypeRequest {

    @NotBlank(message = "walletTypeCode is required") @Size(max = 64) String walletTypeCode;
}
