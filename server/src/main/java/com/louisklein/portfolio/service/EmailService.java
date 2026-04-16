package com.louisklein.portfolio.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "http://localhost:5173/confirmation?token=" + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Verify your Finance Freak account");
        message.setText(
                "Hi,\n\n" +
                        "Thanks for registering with Finance Freak!\n\n" +
                        "Please verify your email address by clicking the link below:\n\n" +
                        verificationUrl + "\n\n" +
                        "This link will expire in 24 hours.\n\n" +
                        "If you didn't create an account, you can ignore this email.\n\n"
        );
        mailSender.send(message);
    }
}