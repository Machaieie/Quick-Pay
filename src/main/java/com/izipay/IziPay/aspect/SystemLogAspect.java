package com.izipay.IziPay.aspect;

import java.time.LocalDateTime;

import org.aspectj.lang.*;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.izipay.IziPay.model.SystemLog;
import com.izipay.IziPay.model.User;
import com.izipay.IziPay.repository.SystemLogRepository;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class SystemLogAspect {

    @Autowired
    private SystemLogRepository systemLogRepository;

    @Autowired
    private HttpServletRequest httpRequest;

    @AfterReturning("@annotation(logAction)")
    public void logAfter(JoinPoint joinPoint, LogAction logAction) {
        Object[] args = joinPoint.getArgs();
        User user = null;

        for (Object arg : args) {
            if (arg instanceof User) {
                user = (User) arg;
                break;
            }
        }

        SystemLog log = new SystemLog();
        log.setAction(logAction.action());
        log.setDetails(logAction.details());
        log.setUser(user);
        log.setTimestamp(LocalDateTime.now());
        log.setIp(httpRequest.getRemoteAddr());

        systemLogRepository.save(log);
    }
}
