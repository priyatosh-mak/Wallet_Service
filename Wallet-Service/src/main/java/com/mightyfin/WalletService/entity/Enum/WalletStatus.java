package com.mightyfin.WalletService.entity.Enum;

public enum WalletStatus {


    PENDING_ACTIVATION,
    ACTIVE,
    /** Was activated once, then switched off. */
    SUSPENDED
}
