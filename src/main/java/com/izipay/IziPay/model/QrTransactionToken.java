package com.izipay.IziPay.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "qr_transaction_token")
public class QrTransactionToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Account account; 

    @Column(nullable = false, unique = true)
    private String token; 

    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private boolean used = false;
}