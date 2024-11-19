package com.sparta.projectblue.domain.email.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;
    private static final String SENDER_EMAIL = "${MAIL_USERNAME}";

    @Async("mailExecutor")
    public void sendMail(String email, String subject, String text) {

        MimeMessage message = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "utf-8");
            helper.setFrom(SENDER_EMAIL); // 보내는 사람
            helper.setTo(email); // 받는 사람
            helper.setSubject(subject); // 이메일 제목
            helper.setText(text, true);
        } catch (MessagingException e) {
            log.error("메일 발송 실패 : {}", e.getMessage());
        }

        javaMailSender.send(message);
    }
}
