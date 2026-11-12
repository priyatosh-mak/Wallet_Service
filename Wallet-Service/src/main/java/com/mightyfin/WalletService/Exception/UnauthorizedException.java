package com.mightyfin.WalletService.Exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends ApiException{

    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", message);
    }

}
