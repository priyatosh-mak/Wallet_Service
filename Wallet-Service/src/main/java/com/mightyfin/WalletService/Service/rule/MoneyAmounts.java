package com.mightyfin.WalletService.Service.rule;

import java.math.BigDecimal;

import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.entity.Currency;
import com.mightyfin.WalletService.entity.Wallet;

public class MoneyAmounts {


    public static final int SCALE = 2;

    public static final BigDecimal MAX_VALUE = Wallet.MAX_BALANCE;

    private MoneyAmounts() {
    }

    public static BigDecimal requireValid(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new BusinessRuleException("INVALID_AMOUNT", "Amount must be greater than zero.");
        }
        if (amount.stripTrailingZeros().scale() > SCALE) {
            throw new BusinessRuleException("INVALID_AMOUNT", "Amount can have at most " + SCALE + " decimal places.");
        }
        BigDecimal normalised = amount.setScale(SCALE);
        if (normalised.compareTo(MAX_VALUE) > 0) {
            throw new BusinessRuleException("INVALID_AMOUNT", "Amount is too large.");
        }
        return normalised;
    }

    public static void requireFitsCurrency(BigDecimal amount, Currency currency) {
        int minorUnits = Math.min(currency.getMinorUnits(), SCALE);
        if (amount.stripTrailingZeros().scale() > minorUnits) {
            throw new BusinessRuleException("INVALID_AMOUNT",
                    currency.getCode() + " amounts can have at most " + minorUnits + " decimal places.");
        }
    }

    public static void requireBalanceRoom(BigDecimal balance, BigDecimal amount) {
        if (balance.add(amount).compareTo(MAX_VALUE) > 0) {
            throw new BusinessRuleException("BALANCE_LIMIT_EXCEEDED", "The receiving wallet cannot hold this amount.");
        }
    }
}
