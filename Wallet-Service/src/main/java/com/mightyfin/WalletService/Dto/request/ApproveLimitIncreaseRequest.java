package com.mightyfin.WalletService.Dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class ApproveLimitIncreaseRequest {


    @DecimalMin(value = "0.01", message = "approvedLimit must be greater than zero")
    @Digits(integer = 16, fraction = 2, message = "approvedLimit allows at most 2 decimal places")
    private BigDecimal approvedLimit;

    @Size(max = 500, message = "note must be at most 500 characters")
    private String note;

    public ApproveLimitIncreaseRequest() {
    }
}
