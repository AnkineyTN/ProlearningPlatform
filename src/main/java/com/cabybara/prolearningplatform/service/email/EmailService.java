package com.cabybara.prolearningplatform.service.email;

public interface EmailService {
    void sendVerifyOtp(String toEmail, String username, String otp);
    void sendResetOtp(String toEmail, String username, String otp);
    void sendPasswordChangedNotification(String toEmail, String username);
    void sendNoteInviteNotification(String toEmail, String inviterName, String noteTitle, String role, String acceptUrl);
}