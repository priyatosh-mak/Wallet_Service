package com.mightyfin.WalletService.Security.exception;


import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.mightyfinance.auth.starter.security.AuthPrincipal;

@Component 
public class WalletSecurityAudit {
private static final Logger AUDIT = LoggerFactory.getLogger("WalletSecurityAudit.class");

    public void accessDenied(AuthPrincipal caller, String reason) {
        AUDIT.warn("event=ACCESS_DENIED user={} session={} role={} reason={}",
                caller.userPublicUuid(), caller.sessionPublicUuid(), caller.role(), reason);
    }

    /** {@code outcome}: MATCHED, MISMATCH, LOCKED or ERROR. */
    public void pinResult(AuthPrincipal caller, String outcome) {
        if ("MATCHED".equals(outcome)) {
            AUDIT.info("event=TXN_PIN user={} session={} outcome={}",
                    caller.userPublicUuid(), caller.sessionPublicUuid(), outcome);
        } else {
            AUDIT.warn("event=TXN_PIN user={} session={} outcome={}",
                    caller.userPublicUuid(), caller.sessionPublicUuid(), outcome);
        }
    }

    public void rateLimited(UUID userPublicUuid) {
        AUDIT.warn("event=RATE_LIMITED user={}", userPublicUuid);
    }

    public void idempotentReplay(UUID ownerPublicUuid, String operation) {
        AUDIT.info("event=IDEMPOTENT_REPLAY user={} operation={}", ownerPublicUuid, operation);
    }

    /** Call from the service layer after a money operation commits. */
    public void moneyOperation(AuthPrincipal caller, String operation, String resourceRef) {
        AUDIT.info("event=MONEY_OP user={} session={} role={} operation={} ref={}",
                caller.userPublicUuid(), caller.sessionPublicUuid(), caller.role(), operation, resourceRef);
    }

    public void adminOperation(AuthPrincipal caller, String operation, String resourceRef) {
        AUDIT.info("event=ADMIN_OP user={} session={} role={} operation={} ref={}",
                caller.userPublicUuid(), caller.sessionPublicUuid(), caller.role(), operation, resourceRef);
    }
}
