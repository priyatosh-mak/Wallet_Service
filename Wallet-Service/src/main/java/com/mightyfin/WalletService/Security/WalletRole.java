package com.mightyfin.WalletService.Security;

import java.util.HashSet;
import java.util.Set;

public final class WalletRole {

    private WalletRole() {
    }

    public static final String SUPER_ADMIN = "super_admin";
    public static final String SUB_ADMIN = "sub_admin";
    public static final String EMBEDDED_ADMIN = "embedded_admin";
    public static final String BUSINESS_ADMIN = "business_admin";
    public static final String BUSINESS_STAFF = "business_staff";
    public static final String EMBEDDED_STAFF = "embedded_staff";
    public static final String INDIVIDUAL = "individual";

    /** Platform operators. */
    public static final Set<String> PLATFORM_ADMINS = Set.of(SUPER_ADMIN, SUB_ADMIN);

    /** Organisation logins: act for the business selected in the session. */
    public static final Set<String> ORGANISATION_ROLES = Set.of(BUSINESS_ADMIN, EMBEDDED_ADMIN);

    /** Member logins: act for the one company they are assigned to. */
    public static final Set<String> STAFF_ROLES = Set.of(BUSINESS_STAFF, EMBEDDED_STAFF);

    /** Every role this service understands. Anything else is refused. */
    public static final Set<String> KNOWN;

    static {
        Set<String> all = new HashSet<>();
        all.addAll(PLATFORM_ADMINS);
        all.addAll(ORGANISATION_ROLES);
        all.addAll(STAFF_ROLES);
        all.add(INDIVIDUAL);
        KNOWN = Set.copyOf(all);
    }
}
