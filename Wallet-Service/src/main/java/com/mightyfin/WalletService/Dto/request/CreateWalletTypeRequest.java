package com.mightyfin.WalletService.Dto.request;

import java.math.BigDecimal;

import com.mightyfin.WalletService.entity.Enum.DisbursementQuotaPeriod;
import com.mightyfin.WalletService.entity.Enum.WalletLimitPeriod;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data 
public class CreateWalletTypeRequest {

    @NotBlank(message = "code is required")
    @Size(max = 64)
    @Pattern(
        regexp = "^[A-Za-z][A-Za-z_]*$",
        message = "code may only contain letters and underscores"
    )
    private String code;

    @NotBlank(message = "name is required")
    @Size(max = 255)
    private String name;

    @Size(max = 2000)
    private String description;

    private Boolean mayEnableSupply;

    private Boolean mayEnableOrigination;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultSupplyQuotaLimit;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultOriginationQuotaLimit;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultCreditLimit;

    private Boolean canExtendCredit;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultDisbursementQuotaLimit;

    private DisbursementQuotaPeriod defaultDisbursementQuotaPeriod;

    @DecimalMin("0.00")
    @Digits(integer = 16, fraction = 2)
    private BigDecimal defaultGeneralTransactionLimit;

    private WalletLimitPeriod defaultGeneralTransactionPeriod;

    

    @Size(max = 255)
    private String valueChainCategory;

    public CreateWalletTypeRequest() {
    }
}
