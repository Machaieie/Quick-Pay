package com.izipay.IziPay.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;
import com.izipay.IziPay.aspect.LogAction;
import com.izipay.IziPay.exceptions.InvalidQrCodeException;
import com.izipay.IziPay.model.Account;
import com.izipay.IziPay.model.QrTransactionToken;
import com.izipay.IziPay.model.enums.SystemAction;
import com.izipay.IziPay.repository.QrTransactionTokenRepository;

@Service
public class TransactionQRCodeService {
    @Autowired
    private QRCodeService qrCodeService;

    @Autowired
    private QrTransactionTokenRepository qrTokenRepository;

    @LogAction(action = SystemAction.QR_CODE_GENERATED, details = "QR Code gerado com sucesso")
    public String generateTransactionQRCode(Account account) throws WriterException, IOException {
        String token = UUID.randomUUID().toString(); 

        QrTransactionToken qrToken = new QrTransactionToken();
        qrToken.setAccount(account);
        qrToken.setToken(token);
        qrToken.setCreatedAt(LocalDateTime.now());
        qrToken.setExpiresAt(LocalDateTime.now().plusMinutes(2)); 
        qrToken.setUsed(false);

        qrTokenRepository.save(qrToken);

        return qrCodeService.generateQRCodeBase64(token);
    }

    public Account validateAndConsumeQRCode(String token) {
    QrTransactionToken qrToken = qrTokenRepository.findByTokenAndUsedFalse(token)
            .orElseThrow(() -> new InvalidQrCodeException("QR Code inválido ou expirado"));

    if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())) {
        throw new RuntimeException("QR Code expirado");
    }

    qrToken.setUsed(true);
    qrTokenRepository.save(qrToken);

    return qrToken.getAccount(); 
}

}
