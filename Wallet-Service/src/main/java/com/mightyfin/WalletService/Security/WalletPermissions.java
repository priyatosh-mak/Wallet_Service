package com.mightyfin.WalletService.Security;

public class WalletPermissions {

    private WalletPermissions() {
    }

    public static final String VIEW = "wallet.view";
    public static final String CASH_IN = "wallet.cash_in";
    public static final String CASH_OUT = "wallet.cash_out";
    public static final String TRANSFER = "wallet.transfer";

    /** PRIVILEGED, non-delegable, super_admin only. */
    public static final String LIMIT_SET = "wallet.limit.set";

    /** super_admin only. Also guard with {@code roles = WalletRoles.SUPER_ADMIN}. */
    public static final String ACTIVATE = "onboarding.wallet.activate";
}
