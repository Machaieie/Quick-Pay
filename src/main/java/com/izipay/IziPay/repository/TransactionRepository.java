package com.izipay.IziPay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.izipay.IziPay.model.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long>{
    
}
