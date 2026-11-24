package com.mightyfin.WalletService.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data 
@AllArgsConstructor 
public class RejectLimitIncreaseRequest {


     @NotBlank(message = "note is required")
    @Size(min = 5, max = 500, message = "note must be between 5 and 500 characters")
    private String note;
}
