package com.mightyfin.WalletService.Config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data 
@ConfigurationProperties (prefix = "wallet.quota-increase")
public class QuotaIncreaseProperties {

    private int expiryDays = 14;

    private int cooldownAfterRejectionHours = 24;

    private int cooldownAfterApprovalHours = 24;

    private BigDecimal maxQuotaRemaining;
}
