package com.izipay.IziPay.controller;

import java.math.BigDecimal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.izipay.IziPay.model.QrTransactionToken;
import com.izipay.IziPay.model.Transaction;
import com.izipay.IziPay.model.dto.request.PaymentRequestDTO;
import com.izipay.IziPay.model.dto.request.TransferRequestDTO;
import com.izipay.IziPay.model.dto.response.PaymentResponseDTO;
import com.izipay.IziPay.model.dto.response.QrTransactionTokenResponse;
import com.izipay.IziPay.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransationController {
     @Autowired
    private TransactionService transactionService;

  
    @PostMapping("/pay")
    public ResponseEntity<PaymentResponseDTO> makePayment(@RequestBody PaymentRequestDTO request) {
        PaymentResponseDTO transaction = transactionService.makePayment(request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/generate-qr")
    public ResponseEntity<QrTransactionTokenResponse> generateQr(
            @RequestParam String username) {
        QrTransactionTokenResponse token = transactionService.generatePaymentQrCode(username);
        return ResponseEntity.ok(token);
    }

@PostMapping("/transfer")
public ResponseEntity<PaymentResponseDTO> transfer(@RequestBody TransferRequestDTO request) {
    try {
        transactionService.transfer(request);
        return ResponseEntity.ok(new PaymentResponseDTO("Transferência realizada com sucesso."));
    } catch (Exception e) {
        return ResponseEntity.badRequest()
                .body(new PaymentResponseDTO("Erro ao processar transferência: " + e.getMessage()));
    }
}

}
