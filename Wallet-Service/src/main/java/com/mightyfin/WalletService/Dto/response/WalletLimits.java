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
    // private BigDecimal generalTransactionLimit;
    // private WalletLimitPeriod generalTransactionPeriod;

    // Limit 1
    private boolean canSupplyOnCredit;
    private BigDecimal supplyQuotaLimit;

    // Limit 2
    private boolean canOriginateLoans;
    private BigDecimal originationQuotaLimit;

    // Pooled credit
    private BigDecimal creditReserved;

    public WalletLimits() {
    }
}
