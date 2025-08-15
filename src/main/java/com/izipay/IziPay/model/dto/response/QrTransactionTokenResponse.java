package com.izipay.IziPay.model.dto.response;

import java.time.LocalDateTime;

public record QrTransactionTokenResponse(
        String token,
        LocalDateTime createdAt,
        LocalDateTime expiresAt) {

}
