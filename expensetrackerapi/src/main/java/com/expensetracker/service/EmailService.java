package com.expensetracker.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import org.springframework.beans.factory.annotation.Value;

@Service 
public class EmailService {
    
    private final JavaMailSender mailSender;
    private final String from;

    public EmailService(JavaMailSender mailSender, @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    public void sendPasswordResetEmail(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Expense Tracker - Redefinir Senha");
        message.setText(
            "Você solicitou uma redefinição de senha.\n\n" +
            "Use o link abaixo para criar uma nova senha.\n" +
            resetLink +
            "\n\nEsse link expira em 30 minutos."
        );

        mailSender.send(message);
    }
}