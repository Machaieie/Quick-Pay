package com.izipay.IziPay.model;

import java.time.LocalDateTime;

import com.izipay.IziPay.model.enums.AttemptStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
@Table(name = "login_attempts")
public class LoginAttempt {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Long attemptId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attempt_status", nullable = false)
    private AttemptStatus status;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "device_info")
    private String deviceInfo;
    
    @Column(name = "attempted_at")
    private LocalDateTime attemptedAt = LocalDateTime.now();
}
