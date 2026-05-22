package com.example.expensemanager.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Đặt lại mật khẩu - Quản lý Chi Tiêu");
        message.setText(
                "Xin chào!\n\n" +
                        "Bạn đã yêu cầu đặt lại mật khẩu.\n\n" +
                        "Click vào link bên dưới để đặt lại mật khẩu:\n" +
                        resetLink + "\n\n" +
                        "Link có hiệu lực trong 30 phút.\n\n" +
                        "Nếu bạn không yêu cầu, hãy bỏ qua email này.\n\n" +
                        "Trân trọng,\nQuản lý Chi Tiêu"
        );
        mailSender.send(message);
    }
}