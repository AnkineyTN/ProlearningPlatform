package com.cabybara.prolearningplatform.service.email;

public interface EmailService {
    void sendVerifyOtp(String toEmail, String username, String otp);
    void sendResetOtp(String toEmail, String username, String otp);
    void sendPasswordChangedNotification(String toEmail, String username);
    void sendNoteInviteNotification(String toEmail, String inviterName, String noteTitle, String role, String acceptUrl);
    void sendFlashcardInviteNotification(String toEmail, String inviterName, String flashcardTitle, String role, String acceptUrl);
    void sendExamInviteNotification(String toEmail, String inviterName, String examTitle, String role, String acceptUrl);
    void sendProUpgradeNotification(String toEmail, String username);
}