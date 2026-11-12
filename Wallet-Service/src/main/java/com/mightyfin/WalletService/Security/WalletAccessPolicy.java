// package com.mightyfin.WalletService.Security;

// import java.util.Objects;

// import org.springframework.stereotype.Component;

// import com.mightyfin.WalletService.Exception.ForbiddenException;
// import com.mightyfin.WalletService.Exception.UnauthorizedException;

// import lombok.extern.slf4j.Slf4j;

// @Slf4j 
// @Component 
// public class WalletAccessPolicy {


//     /**
//      * Super admins, the owning customer, or a caller acting for the owning business may view a wallet.
//      * Returns a boolean rather than throwing so callers can answer "not found" for wallets the caller may not see:
//      * wallet ids are sequential, and a 403 for existing vs 404 for missing would let anyone map them.
//      * Callers must have run {@link #requireAuthenticated} first.
//      */
//     // public boolean canView(Wallet wallet, CallerContext caller) {
//     //     boolean allowed = caller.isAuthenticated() && (caller.isSuperAdmin() || isOwner(wallet, caller));
//     //     if (!allowed) {
//     //         log.warn("Wallet access denied: walletId={} callerUserId={} callerBusiness={}",
//     //                 wallet.getId(), caller.getUserId(), caller.getBusinessPublicUuid());
//     //     }
//     //     return allowed;
//     // }

//     /** Activation, suspension and type changes are back-office operations. */
//     public void requireSuperAdmin(CallerContext caller) {
//         requireAuthenticated(caller);
//         if (!caller.isSuperAdmin()) {
//             log.warn("Admin operation denied: callerUserId={}", caller.getUserId());
//             throw new ForbiddenException("This operation requires the " + CallerContext.SUPER_ADMIN_ROLE + " role");
//         }
//     }

//     /** Creating wallets is for onboarding/back-office callers, never for an end user acting on their own behalf. */
//     public void requireCanProvision(CallerContext caller) {
//         requireAuthenticated(caller);
//         if (!caller.isSuperAdmin() && !caller.isInternalService()) {
//             log.warn("Wallet provisioning denied: callerUserId={} callerBusiness={}",
//                     caller.getUserId(), caller.getBusinessPublicUuid());
//             throw new ForbiddenException("Wallet creation is restricted to internal services and administrators");
//         }
//     }

//     public void requireAuthenticated(CallerContext caller) {
//         if (!caller.isAuthenticated()) {
//             throw new UnauthorizedException("Authentication required");
//         }
//     }

//     public Integer requireUserId(CallerContext caller) {
//         if (caller.getUserId() == null) {
//             throw new UnauthorizedException("Authenticated customer required");
//         }
//         return caller.getUserId();
//     }

//     // private boolean isOwner(Wallet wallet, CallerContext caller) {
//     //     if (wallet.getUserId() != null) {
//     //         return Objects.equals(wallet.getUserId(), caller.getUserId());
//     //     }
//     //     return Objects.equals(wallet.getBusinessPublicUuid(), caller.getBusinessPublicUuid());
//     // }
// }
