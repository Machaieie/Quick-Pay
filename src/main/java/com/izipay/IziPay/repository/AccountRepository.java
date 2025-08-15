package com.izipay.IziPay.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.izipay.IziPay.model.Account;

public interface AccountRepository extends JpaRepository<Account,Long>{
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByQrCodehash(String qrCodeHash);
    boolean existsByAccountNumber(String accountNumber);
    boolean existsByNib(String nib);
    
}
