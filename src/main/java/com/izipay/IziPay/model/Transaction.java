package com.izipay.IziPay.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.izipay.IziPay.model.enums.PaymentMethod;
import com.izipay.IziPay.model.enums.TransationStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Table(name = "Transation")
@Entity
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Account sender;

    @ManyToOne(optional = false)
    private Account recipient;

    @Column(precision = 16, scale = 2, nullable = false)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    private TransationStatus status;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod; 

    private LocalDateTime transationDate;
    private String reference;
}
