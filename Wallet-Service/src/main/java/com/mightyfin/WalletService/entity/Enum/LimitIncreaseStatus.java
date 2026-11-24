package com.mightyfin.WalletService.entity.Enum;

public enum LimitIncreaseStatus {


    PENDING,
    APPROVED,
    REJECTED,
    /** Withdrawn by the requester. */
    CANCELLED,
    /** Nobody decided in time. */
    EXPIRED,
    /** An administrator changed the wallet's limit directly, so the request no longer applies. */
    SUPERSEDED;

    public boolean isFinal() {
        return this != PENDING;
    }
}
