package com.example.demo.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String resetLink) {

        SimpleMailMessage message = new SimpleMailMessage();

        message.setTo(toEmail);

        message.setSubject("Password Reset Request");

        message.setText(
                "You requested to reset your password.\n\n" +
                        "Click the link below:\n\n" +
                        resetLink + "\n\n" +
                        "This link expires in 15 minutes.\n\n" +
                        "If you did not request this, ignore this email."
        );

        mailSender.send(message);
    }
}