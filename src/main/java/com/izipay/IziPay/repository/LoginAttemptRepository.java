package com.izipay.IziPay.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.izipay.IziPay.model.LoginAttempt;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long>{
    
}
