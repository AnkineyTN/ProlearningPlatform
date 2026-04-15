package com.cabybara.prolearningplatform.service.email.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.cabybara.prolearningplatform.dto.request.email.EmailMessage;
import com.cabybara.prolearningplatform.enums.EmailType;
import com.cabybara.prolearningplatform.service.email.EmailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {
 private final RabbitTemplate rabbitTemplate;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queues.transactional}")
    private String transactionalQueue;

    @Value("${app.token.verify-expiry-minutes:10}")
    private int verifyExpiryMinutes;

    @Value("${app.token.reset-expiry-minutes:15}")
    private int resetExpiryMinutes;

    @Value("${app.token.invite-token-expiry-hours:72}")
    private int inviteTokenExpiryHours;

    @Override
    public void sendVerifyOtp(String toEmail, String username, String otp) {
        publish(toEmail, EmailType.VERIFY_OTP, Map.of(
                "username",      username,
                "otp",           otp,
                "expiryMinutes", verifyExpiryMinutes
        ));
    }

    @Override
    public void sendResetOtp(String toEmail, String username, String otp) {
        publish(toEmail, EmailType.RESET_OTP, Map.of(
                "username",      username,
                "otp",           otp,
                "expiryMinutes", resetExpiryMinutes
        ));
    }

    @Override
    public void sendPasswordChangedNotification(String toEmail, String username) {
        publish(toEmail, EmailType.PASSWORD_CHANGED, Map.of(
                "username",     username,
                "changedAt",    LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("HH:mm dd/MM/yyyy")),
                "supportEmail", "support@prolearning.com"
        ));
    }

    @Override
    public void sendNoteInviteNotification(String toEmail, String inviterName, String noteTitle, String role,
            String acceptUrl) {
        publish(toEmail, EmailType.NOTE_INVITE, Map.of(
                "inviterName", inviterName,
                "noteTitle",   noteTitle,
                "role",        role,
                "acceptUrl",   acceptUrl,
                "expiryHours",  inviteTokenExpiryHours
        ));
    }

    private void publish(String toEmail, EmailType type, Map<String, Object> variables) {
        EmailMessage message = EmailMessage.builder()
                .to(toEmail)
                .subject(type.getSubject())
                .template(type.getTemplate())
                .variables(variables)
                .build();

        rabbitTemplate.convertAndSend(exchange, transactionalQueue, message);
        log.info("Email queued | type={} to={}", type, toEmail);
    }
}
