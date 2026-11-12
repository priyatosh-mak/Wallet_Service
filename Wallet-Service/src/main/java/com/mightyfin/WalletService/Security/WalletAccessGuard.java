package com.mightyfin.WalletService.Security;

import java.util.Objects;
import java.util.UUID;

import com.mightyfin.WalletService.Security.exception.WalletSecurityAudit;
import com.mightyfin.WalletService.Security.exception.WalletSecurityException;
import com.mightyfinance.auth.starter.security.AuthPrincipal;

public class WalletAccessGuard {

    private final WalletSecurityAudit audit;

    public WalletAccessGuard(WalletSecurityAudit audit) {
        this.audit = audit;
    }

    /** Throws {@link WalletSecurityException.WalletNotFound} when the caller may not access the wallet. */
    // public void assertCanAccess(AuthPrincipal caller, WalletScope wallet) {
    //     Objects.requireNonNull(caller, "caller must not be null");
    //     Objects.requireNonNull(wallet, "wallet must not be null");
    //     String reason = denialReason(caller, wallet);
    //     if (reason != null) {
    //         audit.accessDenied(caller, reason);
    //         throw new WalletSecurityException.WalletNotFound();
    //     }
    // }

    /**
     * Back-office operations that no permission code covers yet (reference data, wallet creation, type
     * change). Defence in depth behind {@code @RequiresPermission(roles = WalletRoles.SUPER_ADMIN)}: the
     * service layer still refuses the call if a controller is ever added without the annotation.
     */
    public void requireSuperAdmin(AuthPrincipal caller) {
        Objects.requireNonNull(caller, "caller must not be null");
        if (!WalletRole.SUPER_ADMIN.equals(caller.role())) {
            audit.accessDenied(caller, "super_admin_required");
            throw new WalletSecurityException.Forbidden();
        }
    }

    /** Platform admins never move customer or business money, whatever permissions they hold. */
    public void requireNotPlatformAdmin(AuthPrincipal caller) {
        Objects.requireNonNull(caller, "caller must not be null");
        if (WalletRole.PLATFORM_ADMINS.contains(caller.role())) {
            audit.accessDenied(caller, "admin_cannot_move_money");
            throw new WalletSecurityException.Forbidden();
        }
    }

    /** Non-throwing variant, for filtering an already loaded collection. */
    // public boolean canAccess(AuthPrincipal caller, WalletScope wallet) {
    //     return denialReason(caller, wallet) == null;
    // }

    /**
     * The filter to put in the WHERE clause of list and search queries, so other people's wallets
     * are never loaded. Throws when the caller has no usable scope.
     */
    public QueryScope queryScope(AuthPrincipal caller) {
        Objects.requireNonNull(caller, "caller must not be null");
        String role = caller.role();

        if (WalletRole.PLATFORM_ADMINS.contains(role)) {
            return new QueryScope(true, null, null, null);
        }
        if (WalletRole.INDIVIDUAL.equals(role)) {
            if (caller.userId() == null) {
                audit.accessDenied(caller, "no_query_scope");
                throw new WalletSecurityException.WalletNotFound();
            }
            return new QueryScope(false, caller.userId(), null, null);
        }
        if (WalletRole.ORGANISATION_ROLES.contains(role)) {
            if (caller.activeBusinessPublicUuid() == null) {
                throw new WalletSecurityException.BusinessContextRequired();
            }
            return new QueryScope(false, null, caller.activeBusinessPublicUuid(), caller.tenantPublicUuid());
        }
        if (WalletRole.STAFF_ROLES.contains(role) && caller.assignedCompanyPublicUuid() != null) {
            return new QueryScope(false, null, caller.assignedCompanyPublicUuid(), caller.tenantPublicUuid());
        }
        audit.accessDenied(caller, "no_query_scope");
        throw new WalletSecurityException.WalletNotFound();
    }

    // private static String denialReason(AuthPrincipal caller, WalletScope wallet) {
    //     String role = caller.role();

    //     if (WalletRole.PLATFORM_ADMINS.contains(role)) {
    //         return null;
    //     }
    //     if (WalletRole.INDIVIDUAL.equals(role)) {
    //         boolean own = wallet.businessPublicUuid() == null
    //                 && caller.userId() != null
    //                 && caller.userId().equals(wallet.ownerUserId());
    //         return own ? null : "not_owner";
    //     }
    //     if (WalletRole.ORGANISATION_ROLES.contains(role)) {
    //         return businessMatches(caller.activeBusinessPublicUuid(), caller, wallet) ? null : "business_mismatch";
    //     }
    //     if (WalletRole.STAFF_ROLES.contains(role)) {
    //         return businessMatches(caller.assignedCompanyPublicUuid(), caller, wallet) ? null : "company_mismatch";
    //     }
    //     return "unknown_role";
    // }

    // private static boolean businessMatches(UUID callerBusiness, AuthPrincipal caller, WalletScope wallet) {
    //     if (callerBusiness == null || !callerBusiness.equals(wallet.businessPublicUuid())) {
    //         return false;
    //     }
    //     return wallet.tenantPublicUuid() == null || wallet.tenantPublicUuid().equals(caller.tenantPublicUuid());
    // }

    /**
     * Repository filter derived from the caller.
     *
     * @param unrestricted        true for platform admins (no ownership filter)
     * @param ownerUserId        set for {@code individual}: only customer wallets of this Auth user id
     * @param businessPublicUuid  set for organisation and staff logins: only wallets of this business
     * @param tenantPublicUuid    when set together with a business, the wallet's tenant must match
     */
    public record QueryScope(boolean unrestricted, Integer ownerUserId,
                             UUID businessPublicUuid, UUID tenantPublicUuid) {
    }

}
