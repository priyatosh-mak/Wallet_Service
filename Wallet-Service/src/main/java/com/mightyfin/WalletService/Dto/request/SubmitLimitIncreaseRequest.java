package com.mightyfin.WalletService.Dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class SubmitLimitIncreaseRequest {


    @NotNull (message = "requestedLimit is required")
    @DecimalMin(value = "0.01", message = "requestedLimit must be greater than zero")
    @Digits(integer = 16, fraction = 2, message = "requestedLimit allows at most 2 decimal places")
    private BigDecimal requestedLimit;

    @NotBlank(message = "reason is required")
    @Size(min = 10, max = 500, message = "reason must be between 10 and 500 characters")
    private String reason;

    public SubmitLimitIncreaseRequest() {
    }
}
