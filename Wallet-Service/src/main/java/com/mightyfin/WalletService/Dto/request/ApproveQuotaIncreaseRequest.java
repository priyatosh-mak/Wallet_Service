package com.mightyfin.WalletService.Dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
@Data 
@AllArgsConstructor 
public class ApproveQuotaIncreaseRequest {


    @DecimalMin(
            value = "0.01",
            message = "approvedIncrease must be greater than zero"
    )
    @Digits(
            integer = 16,
            fraction = 2,
            message = "approvedIncrease allows at most 2 decimal places"
    )
    private BigDecimal approvedIncrease;

    @Size(
            max = 500,
            message = "note must be at most 500 characters"
    )
    private String note;
}
