package com.izipay.IziPay.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.izipay.IziPay.model.SystemLog;

public interface SystemLogRepository extends JpaRepository<SystemLog, Long> {
}