package com.mightyfin.WalletService.Dto.response;

import java.math.BigDecimal;

import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class WalletTypeResponse {

    private Integer id;
    private String code;
    private String name;
    private String description;
    private BigDecimal defaultCreditLimit;
    private boolean canExtendCredit;
    private BigDecimal defaultDisbursementQuotaLimit;
    private DisbursementQuotaPeriod defaultDisbursementQuotaPeriod;
    private BigDecimal defaultGeneralTransactionLimit;
    private WalletLimitPeriod defaultGeneralTransactionPeriod;
    private String valueChainCategory;
    private boolean active;
    private boolean mayEnableSupply;
    private boolean mayEnableOrigination;
    private BigDecimal defaultSupplyQuotaLimit;
    private BigDecimal defaultOriginationQuotaLimit;

    public WalletTypeResponse() {
    }
}
