package com.mora.digitalwalletledgerservice.exception;


public class AccessDeniedRequestException extends RuntimeException {

    public AccessDeniedRequestException(String message) {
        super(message);
    }
}
