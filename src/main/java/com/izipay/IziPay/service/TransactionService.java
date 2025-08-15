package com.izipay.IziPay.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.zxing.WriterException;
import com.izipay.IziPay.aspect.LogAction;
import com.izipay.IziPay.exceptions.InvalidQrCodeException;
import com.izipay.IziPay.exceptions.UserNotFoundException;
import com.izipay.IziPay.model.Account;
import com.izipay.IziPay.model.QrTransactionToken;
import com.izipay.IziPay.model.Transaction;
import com.izipay.IziPay.model.User;
import com.izipay.IziPay.model.dto.request.PaymentRequestDTO;
import com.izipay.IziPay.model.dto.response.PaymentResponseDTO;
import com.izipay.IziPay.model.dto.response.QrTransactionTokenResponse;
import com.izipay.IziPay.model.enums.SystemAction;
import com.izipay.IziPay.model.enums.TransationStatus;
import com.izipay.IziPay.repository.AccountRepository;
import com.izipay.IziPay.repository.QrTransactionTokenRepository;
import com.izipay.IziPay.repository.TransactionRepository;
import com.izipay.IziPay.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class TransactionService {

    @Autowired
    private QrTransactionTokenRepository qrTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private QRCodeService qrCodeService;

    @LogAction(action = SystemAction.TRANSATION_SUCESSFULLY, details = "Transacao efectuada com sucesso")
    @Transactional
    public PaymentResponseDTO makePayment(PaymentRequestDTO request) {
        log.info("Iniciando transação do usuário '{}' para QR Token '{}', valor: {}",
                request.senderUsername(), request.qrToken(), request.amount());

        User sender = userRepository.findByUsername(request.senderUsername())
                .orElseThrow(() -> {
                    log.error("Usuário remetente '{}' não encontrado", request.senderUsername());
                    return new UserNotFoundException("Remetente não encontrado");
                });

        if (!passwordEncoder.matches(request.pin(), sender.getPin())) {
            log.warn("PIN incorreto para usuário '{}'", sender.getUsername());
            throw new RuntimeException("PIN incorreto");
        }

        QrTransactionToken qrToken = qrTokenRepository.findByTokenAndUsedFalse(request.qrToken())
                .orElseThrow(() -> {
                    log.error("QR Code inválido ou já utilizado: {}", request.qrToken());
                    return new InvalidQrCodeException("QR Code inválido ou já utilizado");
                });

        if (qrToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("QR Code expirado: {}", request.qrToken());
            throw new RuntimeException("QR Code expirado");
        }

        Account recipientAccount = qrToken.getAccount();
        Account senderAccount = sender.getAccount();

        if (senderAccount.getBalance().compareTo(request.amount()) < 0) {
            log.warn("Saldo insuficiente. Saldo atual: {}, Valor solicitado: {}",
                    senderAccount.getBalance(), request.amount());
            throw new RuntimeException("Saldo insuficiente");
        }

        senderAccount.setBalance(senderAccount.getBalance().subtract(request.amount()));
        recipientAccount.setBalance(recipientAccount.getBalance().add(request.amount()));

        accountRepository.save(senderAccount);
        accountRepository.save(recipientAccount);

        qrToken.setUsed(true);
        qrTokenRepository.save(qrToken);

        Transaction transaction = new Transaction();
        transaction.setSender(senderAccount);
        transaction.setRecipient(recipientAccount);
        transaction.setValue(request.amount());
        transaction.setStatus(TransationStatus.SUCCESS);
        transaction.setTransationDate(LocalDateTime.now());
        transaction.setReference("TX-" + System.currentTimeMillis());

        transactionRepository.save(transaction);

        return new PaymentResponseDTO("Pagamento realizado com sucesso.");
    }

    /**
     * Gera um QR Code (token) para recebimento de pagamentos.
     * 
     * @param username nome de usuário do receptor
     * @param amount   valor esperado (opcional, pode ser null para flexível)
     * @return token gerado
     */
    @Transactional
    public QrTransactionTokenResponse generatePaymentQrCode(String username) {
        log.info("Gerando QR Code de pagamento para usuário '{}'", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Usuário '{}' não encontrado para gerar QR Code", username);
                    return new UserNotFoundException("Usuário não encontrado");
                });

        QrTransactionToken token = new QrTransactionToken();
        token.setToken(UUID.randomUUID().toString());
        token.setAccount(user.getAccount());
        token.setUsed(false);
        token.setCreatedAt(LocalDateTime.now());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(5));

        qrTokenRepository.save(token);

        log.info("QR Code gerado com sucesso. Token: {}", token.getToken());

     
        String qrCodeBase64 = "";
        try {
            qrCodeBase64 = qrCodeService.generateQRCodeBase64(token.getToken());
        } catch (WriterException | IOException e) {
            log.error("Erro ao gerar QR Code em Base64", e);
        }

        return new QrTransactionTokenResponse(
                token.getToken(),
                qrCodeBase64,
                token.getCreatedAt(),
                token.getExpiresAt()
                );
    }

}
