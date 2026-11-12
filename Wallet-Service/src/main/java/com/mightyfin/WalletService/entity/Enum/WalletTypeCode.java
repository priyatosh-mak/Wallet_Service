package com.mightyfin.WalletService.entity.Enum;

public enum WalletTypeCode {
     CUSTOMER, ORGANIZATION, PARTNER, SYSTEM;

     public static boolean isReserved(String code) {
        if (code == null) {
            return false;
        }
        for (WalletTypeCode reserved : values()) {
            if (reserved.name().equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}
