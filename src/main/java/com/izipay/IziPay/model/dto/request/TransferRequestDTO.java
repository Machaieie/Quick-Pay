package com.izipay.IziPay.model.dto.request;

import java.math.BigDecimal;

public record TransferRequestDTO(
        String pin,
        String recipientAccountNumber,
        BigDecimal amount) {

}
