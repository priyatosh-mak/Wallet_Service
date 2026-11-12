package com.mightyfin.WalletService.Exception;

import org.springframework.http.HttpStatus;

/** The caller is known but is not allowed to perform this operation. */
public class ForbiddenException extends ApiException {
    
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, "ACCESS_DENIED", message);
    }

}
