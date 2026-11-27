package com.mightyfin.WalletService.Dto.request;

import java.math.BigDecimal;

import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data 
@AllArgsConstructor 
public class UpdateWalletTypeRequest {


    @NotBlank(message = "name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultCreditLimit;
    @NotNull(message = "mayEnableSupply is required")
    private Boolean mayEnableSupply;

    @NotNull(message = "mayEnableOrigination is required")
    private Boolean mayEnableOrigination;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultSupplyQuotaLimit;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultOriginationQuotaLimit;
    @NotNull (message = "canExtendCredit is required")
    private Boolean canExtendCredit;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultDisbursementQuotaLimit;

    @NotNull(message = "defaultDisbursementQuotaPeriod is required")
    private DisbursementQuotaPeriod defaultDisbursementQuotaPeriod;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultGeneralTransactionLimit;

    @NotNull(message = "defaultGeneralTransactionPeriod is required")
    private WalletLimitPeriod defaultGeneralTransactionPeriod;

    @Size(max = 255)
    private String valueChainCategory;

}
