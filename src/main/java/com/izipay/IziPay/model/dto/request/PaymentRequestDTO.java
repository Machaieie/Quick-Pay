package com.izipay.IziPay.model.dto.request;

import java.math.BigDecimal;

public record PaymentRequestDTO(
        String pin,
        String qrToken,
        BigDecimal amount
) {}