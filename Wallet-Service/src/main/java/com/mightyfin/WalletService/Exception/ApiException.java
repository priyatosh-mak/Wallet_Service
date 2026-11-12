package com.mightyfin.WalletService.Exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter 
public class ApiException extends RuntimeException{

    private final HttpStatus status;
    private final String errorCode;

    protected ApiException(HttpStatus status , String errorCoode, String message){
        super(message);
        this.status=status;
        this.errorCode=errorCoode;
    }

}
