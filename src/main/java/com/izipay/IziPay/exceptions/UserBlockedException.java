package com.izipay.IziPay.exceptions;


public class UserBlockedException extends RuntimeException {
    public UserBlockedException(String message) {
        super(message);
    }
    
    public UserBlockedException(String message, Throwable cause) {
        super(message, cause);
    }
}