package com.izipay.IziPay.exceptions;

public class AmountExceedsLimitException extends RuntimeException {
    public AmountExceedsLimitException(String message) { super(message); }
}
