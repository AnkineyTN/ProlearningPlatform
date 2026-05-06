package com.cabybara.prolearningplatform.service.otp;

public interface OtpService {
    String generateVerifyOtp(Long userId);
    String generateResetOtp(Long userId);
    void verifyVerifyOtp(Long userId, String inputOtp);
    void verifyResetOtp(Long userId, String inputOtp);
    String generateResetToken(Long userId);
    Long validateAndConsumeResetToken(String token);
}
