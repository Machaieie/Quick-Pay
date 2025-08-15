package com.izipay.IziPay.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class UserBlockedException extends RuntimeException {
    public UserBlockedException(String message) {
        super(message);
    }
    
    public UserBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}