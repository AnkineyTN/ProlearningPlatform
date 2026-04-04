package com.cabybara.prolearningplatform.service.email;

public interface EmailService {
    void sendVerifyOtp(String toEmail, String username, String otp);
    void sendResetOtp(String toEmail, String username, String otp);
    void sendPasswordChangedNotification(String toEmail, String username);
}