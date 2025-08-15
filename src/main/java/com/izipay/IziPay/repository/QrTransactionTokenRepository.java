package com.izipay.IziPay.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.izipay.IziPay.model.QrTransactionToken;

public interface QrTransactionTokenRepository extends JpaRepository<QrTransactionToken, Long> {
    Optional<QrTransactionToken> findByTokenAndUsedFalse(String token);
}