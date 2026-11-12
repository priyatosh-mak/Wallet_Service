package com.mightyfin.WalletService.Exception;

import org.springframework.http.HttpStatus;

public class BusinessRuleException extends ApiException{

    public BusinessRuleException(String message) {
        this("BUSINESS_RULE_VIOLATION", message);
    }

    public BusinessRuleException(String errorCode, String message) {
        super(HttpStatus.UNPROCESSABLE_ENTITY, errorCode, message);
    }
}
