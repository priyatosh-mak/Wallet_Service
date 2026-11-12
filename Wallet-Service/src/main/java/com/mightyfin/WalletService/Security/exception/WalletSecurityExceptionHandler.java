package com.mightyfin.WalletService.Security.exception;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mightyfin.WalletService.Dto.response.ApiResponse;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class WalletSecurityExceptionHandler {

    @ExceptionHandler(WalletSecurityException.class)
    public ResponseEntity<ApiResponse<Void>> handle(WalletSecurityException ex) {
        String message = ex.getMessage();
        if (ex instanceof WalletSecurityException.PinInvalid pin) {
            message = message + ". Attempts remaining: " + pin.attemptsRemaining();
        }
        return ResponseEntity.status(ex.status()).body(ApiResponse.error(ex.code(), message));
    }
}
