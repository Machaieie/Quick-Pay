package com.izipay.IziPay.service;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.izipay.IziPay.model.Email;
import com.izipay.IziPay.model.enums.EmailStatus;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    

    @Autowired  
    private JavaMailSender emailSender;

    public String sendEmail(Email emailDetails) {
        emailDetails.setSendDateEmail(LocalDateTime.now());
        try{
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(emailDetails.getEmailFrom());
            message.setTo(emailDetails.getEmailTo());
            message.setSubject(emailDetails.getSubject());
            message.setText(emailDetails.getText());
            emailSender.send(message);

            emailDetails.setStatusEmail(EmailStatus.SENT);
        } catch (MailException e){
            emailDetails.setStatusEmail(EmailStatus.ERROR);
        } finally {
           return "email enviado com sucesso";
        }
    }

     @Async
	public void send(String to, String subject, String mail) {

		try {

			MimeMessage mimeMessage = emailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
			helper.setTo(to);
			helper.setFrom("prmmozambique@gmail.com", "EMAIL TESTE");
			helper.setSubject(subject);
			helper.setText(mail, true);
			emailSender.send(mimeMessage);

		} catch (Exception e) {
			//throw new BusinessException("Ocorreu um erro ao enviar o email");
		}
	}
}