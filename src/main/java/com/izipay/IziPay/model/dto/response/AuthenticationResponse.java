package com.izipay.IziPay.model.dto.response;

public record AuthenticationResponse(
    String accessToken,
    String refreshToken,
    String message
){
    
}
