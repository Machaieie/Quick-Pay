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
import com.izipay.IziPay.model.dto.response.QrTransactionTokenResponse;
import com.izipay.IziPay.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransationController {
     @Autowired
    private TransactionService transactionService;

  
    @PostMapping("/pay")
    public ResponseEntity<Transaction> makePayment(@RequestBody PaymentRequestDTO request) {
        Transaction transaction = transactionService.makePayment(request);
        return ResponseEntity.ok(transaction);
    }

    @PostMapping("/generate-qr")
    public ResponseEntity<QrTransactionTokenResponse> generateQr(
            @RequestParam String username,
            @RequestParam(required = false) BigDecimal amount) {
        QrTransactionTokenResponse token = transactionService.generatePaymentQrCode(username, amount);
        return ResponseEntity.ok(token);
    }
}
