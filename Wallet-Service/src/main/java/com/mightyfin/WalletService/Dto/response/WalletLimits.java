package com.mightyfin.WalletService.Dto.response;

import java.math.BigDecimal;

import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class WalletLimits {

    private BigDecimal generalTransactionLimit;
    private WalletLimitPeriod generalTransactionPeriod;
    private BigDecimal disbursementQuotaLimit;
    private BigDecimal disbursementQuotaUsed;
    private DisbursementQuotaPeriod disbursementQuotaPeriod;

    public WalletLimits() {
    }
}
