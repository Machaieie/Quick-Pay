package com.izipay.IziPay.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.izipay.IziPay.model.LoginAttempt;
import com.izipay.IziPay.model.User;
import com.izipay.IziPay.model.enums.AttemptStatus;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Long>{

    long countByUserAndStatusAndAttemptedAtAfter(User user, AttemptStatus failed, LocalDateTime minusHours);
    
}
