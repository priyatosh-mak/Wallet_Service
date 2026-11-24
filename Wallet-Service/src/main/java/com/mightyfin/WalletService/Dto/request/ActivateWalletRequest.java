package com.mightyfin.WalletService.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class ActivateWalletRequest {

    @NotBlank(message = "approvalReference is required")
    @Size(max = 100, message = "approvalReference must be at most 100 characters") String approvalReference;
}
