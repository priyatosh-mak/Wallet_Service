package com.mightyfin.WalletService.Security.exception;

import org.springframework.http.HttpStatus;

public class WalletSecurityException extends RuntimeException {

    private final HttpStatus status;
    private final String code;

    protected WalletSecurityException(HttpStatus status, String code, String message) {
        super(message, null, false, false);
        this.status = status;
        this.code = code;
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }

    /** Wallet does not exist, or the caller may not see it (deliberately indistinguishable). */
    public static final class WalletNotFound extends WalletSecurityException {
        public WalletNotFound() {
            super(HttpStatus.NOT_FOUND, "WALLET_NOT_FOUND", "Wallet not found");
        }
    }

    /** The caller's role may not perform this operation at all. */
    public static final class Forbidden extends WalletSecurityException {
        public Forbidden() {
            super(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied");
        }
    }

    /** Organisation login without an active business. */
    public static final class BusinessContextRequired extends WalletSecurityException {
        public BusinessContextRequired() {
            super(HttpStatus.FORBIDDEN, "BUSINESS_CONTEXT_REQUIRED", "Select a business to continue.");
        }
    }

    public static final class PinRequired extends WalletSecurityException {
        public PinRequired() {
            super(HttpStatus.BAD_REQUEST, "TRANSACTION_PIN_REQUIRED", "Transaction PIN is required");
        }
    }

    /** 403, not 401: a wrong PIN must not make clients treat the session as expired and log out. */
    public static final class PinInvalid extends WalletSecurityException {
        private final int attemptsRemaining;

        public PinInvalid(int attemptsRemaining) {
            super(HttpStatus.FORBIDDEN, "TRANSACTION_PIN_INVALID", "Transaction PIN is incorrect");
            this.attemptsRemaining = attemptsRemaining;
        }

        public int attemptsRemaining() {
            return attemptsRemaining;
        }
    }

    public static final class PinLocked extends WalletSecurityException {
        public PinLocked() {
            super(HttpStatus.LOCKED, "TRANSACTION_PIN_LOCKED", "Transaction PIN is locked. Try again later.");
        }
    }

    /** Auth could not answer. Fail closed: the money operation must not proceed. */
    public static final class PinUnavailable extends WalletSecurityException {
        public PinUnavailable() {
            super(HttpStatus.SERVICE_UNAVAILABLE, "PIN_VERIFICATION_UNAVAILABLE",
                    "Transaction PIN could not be verified");
        }
    }

    public static final class IdempotencyKeyRequired extends WalletSecurityException {
        public IdempotencyKeyRequired() {
            super(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_REQUIRED", "Idempotency-Key header is required");
        }
    }

    public static final class IdempotencyKeyInvalid extends WalletSecurityException {
        public IdempotencyKeyInvalid() {
            super(HttpStatus.BAD_REQUEST, "IDEMPOTENCY_KEY_INVALID",
                    "Idempotency key must be 16-80 characters: letters, digits, '-' or '_'");
        }
    }

    /** Same key sent with a different operation or payload. */
    public static final class IdempotencyKeyReused extends WalletSecurityException {
        public IdempotencyKeyReused() {
            super(HttpStatus.UNPROCESSABLE_ENTITY, "IDEMPOTENCY_KEY_REUSED",
                    "Idempotency key was already used with a different request");
        }
    }

    /** A concurrent request with the same key is still running. The client should retry. */
    public static final class RequestInProgress extends WalletSecurityException {
        public RequestInProgress() {
            super(HttpStatus.CONFLICT, "REQUEST_IN_PROGRESS",
                    "A request with this key is being processed. Retry shortly.");
        }
    }

}
