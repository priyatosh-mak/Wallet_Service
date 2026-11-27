package com.mightyfin.WalletService.entity.Enum;

public enum CreditReservationStatus {

    RESERVED,
    /** Disbursed: the quota is used and the loan is Loan's outstanding. Final. */
    CONSUMED,
    /** Declined or cancelled before disbursement; the capacity was given back. Final. */
    RELEASED,
    /** Nobody decided in time; the capacity was given back. Final. */
    EXPIRED
}
