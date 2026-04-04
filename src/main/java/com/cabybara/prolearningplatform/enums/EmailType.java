package com.cabybara.prolearningplatform.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EmailType {

    VERIFY_OTP      ("Your email verification code", "email/verify-otp"),
    RESET_OTP       ("Your password reset code",     "email/reset-otp"),
    PASSWORD_CHANGED("Your password has been changed","email/password-changed");

    private final String subject;
    private final String template;
}