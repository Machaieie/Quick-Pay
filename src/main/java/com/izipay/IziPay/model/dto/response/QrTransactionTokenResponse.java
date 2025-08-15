package com.izipay.IziPay.model.dto.response;

import java.time.LocalDateTime;

public record QrTransactionTokenResponse(
        String token,
         String qrCodeBase64,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

}
