package com.mightyfin.WalletService.Dto.request;

import java.math.BigDecimal;

import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data 
public class UpdateWalletLimitRequest {
    @NotNull (message = "generalTransactionLimit is required")
    @DecimalMin(value = "0.00", message = "generalTransactionLimit cannot be negative")
    @Digits(integer = 16, fraction = 2, message = "generalTransactionLimit allows at most 2 decimal places")
    private BigDecimal generalTransactionLimit;

    @NotNull(message = "generalTransactionPeriod is required")
    private WalletLimitPeriod generalTransactionPeriod;

    public UpdateWalletLimitRequest() {
    }

    public UpdateWalletLimitRequest(BigDecimal generalTransactionLimit,
                                    WalletLimitPeriod generalTransactionPeriod) {
        this.generalTransactionLimit = generalTransactionLimit;
        this.generalTransactionPeriod = generalTransactionPeriod;
    }

    
}
