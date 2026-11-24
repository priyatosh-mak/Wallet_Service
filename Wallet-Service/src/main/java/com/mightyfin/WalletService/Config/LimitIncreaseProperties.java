package com.mightyfin.WalletService.Config;

import java.math.BigDecimal;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component 
@ConfigurationProperties (prefix = "wallet.limit-increase")
public class LimitIncreaseProperties {

    private int expiryDays = 14;

    /**
     * After a rejection, the wallet cannot ask again for this long.
     */
    private int cooldownAfterRejectionHours = 24;

    /**
     * After an approval, the wallet cannot ask again for this long.
     */
    private int cooldownAfterApprovalHours = 24;

    /**
     * Optional hard ceiling for any requested or approved limit.
     * Null means only the column capacity applies.
     */
    private BigDecimal maxGeneralLimit;

    public int getExpiryDays() {
        return expiryDays;
    }

    public void setExpiryDays(int expiryDays) {
        this.expiryDays = expiryDays;
    }

    public int getCooldownAfterRejectionHours() {
        return cooldownAfterRejectionHours;
    }

    public void setCooldownAfterRejectionHours(int cooldownAfterRejectionHours) {
        this.cooldownAfterRejectionHours = cooldownAfterRejectionHours;
    }

    public int getCooldownAfterApprovalHours() {
        return cooldownAfterApprovalHours;
    }

    public void setCooldownAfterApprovalHours(int cooldownAfterApprovalHours) {
        this.cooldownAfterApprovalHours = cooldownAfterApprovalHours;
    }

    public BigDecimal getMaxGeneralLimit() {
        return maxGeneralLimit;
    }

    public void setMaxGeneralLimit(BigDecimal maxGeneralLimit) {
        this.maxGeneralLimit = maxGeneralLimit;
    }
}
