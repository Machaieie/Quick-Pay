package com.izipay.IziPay.exceptions;


public class TransactionPrecisionException extends RuntimeException {
    public TransactionPrecisionException(String message) {
        super(message);
    }
    
    public TransactionPrecisionException(String message, Throwable cause) {
        super(message, cause);
    }
}