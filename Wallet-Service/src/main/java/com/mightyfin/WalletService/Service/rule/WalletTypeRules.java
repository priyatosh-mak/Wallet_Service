package com.mightyfin.WalletService.Service.rule;

import com.mightyfin.WalletService.Exception.BusinessRuleException;
import com.mightyfin.WalletService.entity.WalletType;
import com.mightyfin.WalletService.entity.Enum.WalletTypeCode;

public final class WalletTypeRules {
    private static final String ERROR_CODE = "WALLET_TYPE_NOT_ALLOWED";

    private WalletTypeRules() {
    }

    public static void requireCompatible(WalletType type, boolean customerWallet) {
        boolean customerType = type.hasCode(WalletTypeCode.CUSTOMER);
        if (customerWallet && !customerType) {
            throw new BusinessRuleException(ERROR_CODE, "A customer wallet must have the CUSTOMER wallet type");
        }
        if (!customerWallet && customerType) {
            throw new BusinessRuleException(ERROR_CODE, "A business wallet cannot have the CUSTOMER wallet type");
        }
    }
}
