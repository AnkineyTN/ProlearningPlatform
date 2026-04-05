package com.cabybara.prolearningplatform.service.otp.impl;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.cabybara.prolearningplatform.exception.OtpException;
import com.cabybara.prolearningplatform.service.otp.OtpService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpServiceImpl implements OtpService {
    private final StringRedisTemplate redis;

    private static final int      MAX_ATTEMPTS     = 5;
    private static final Duration VERIFY_TTL       = Duration.ofMinutes(10);
    private static final Duration RESET_TTL        = Duration.ofMinutes(15);
    private static final Duration RESET_TOKEN_TTL  = Duration.ofMinutes(5);
    private static final Duration RESEND_LOCK_TTL  = Duration.ofSeconds(60);
    private static final Duration ATTEMPT_TTL      = Duration.ofMinutes(15);

    private String verifyKey(Long userId)              { return "otp:verify:" + userId; }
    private String resetKey(Long userId)               { return "otp:reset:"  + userId; }
    private String attemptsKey(String key)             { return key + ":attempts"; }
    private String resendLockKey(Long userId, String type) {
       return "otp:resend-lock:" + type + ":" + userId;
    }
    private String resetTokenKey(String token)         { return "reset-token:" + token; }

    @Override
    public String generateVerifyOtp(Long userId) {
       return generate(userId, verifyKey(userId), VERIFY_TTL, "verify");
    }

    @Override
    public String generateResetOtp(Long userId) {
       return generate(userId, resetKey(userId), RESET_TTL, "reset");
    }

    private String generate(Long userId, String key, Duration ttl, String type) {
       String lockKey = resendLockKey(userId, type);
       if (Boolean.TRUE.equals(redis.hasKey(lockKey))) {
          Long remaining = redis.getExpire(lockKey);
          throw new OtpException(
             "Please wait " + (remaining != null ? remaining : 60) + " seconds before resending."
          );
       }

       String otp = randomOtp();
       redis.opsForValue().set(key,              otp,  ttl);
       redis.opsForValue().set(attemptsKey(key), "0",  ATTEMPT_TTL);
       redis.opsForValue().set(lockKey,          "1",  RESEND_LOCK_TTL);

       log.debug("OTP generated | userId={} type={}", userId, type);
       return otp;
    }

    @Override
    public void verifyVerifyOtp(Long userId, String inputOtp) {
       verify(userId, verifyKey(userId), inputOtp);
    }

    @Override
    public void verifyResetOtp(Long userId, String inputOtp) {
       verify(userId, resetKey(userId), inputOtp);
    }

    private void verify(Long userId, String key, String inputOtp) {
       String attKey = attemptsKey(key);

       String stored = redis.opsForValue().get(key);
       if (stored == null) {
          throw new OtpException("OTP has expired or does not exist. Please request a new code.");
       }

       String attStr = redis.opsForValue().get(attKey);
       int attempts = attStr != null ? Integer.parseInt(attStr) : 0;
       if (attempts >= MAX_ATTEMPTS) {
          redis.delete(key);
          redis.delete(attKey);
          throw new OtpException("Exceeded maximum attempts of " + MAX_ATTEMPTS + ". Please request a new code.");
       }

       if (!stored.equals(inputOtp)) {
          redis.opsForValue().increment(attKey);
          int remaining = MAX_ATTEMPTS - attempts - 1;
          throw new OtpException("Incorrect OTP. " + remaining + " attempts remaining.");
       }

       redis.delete(key);
       redis.delete(attKey);
       log.debug("OTP verified | userId={}", userId);
    }

    @Override
    public String generateResetToken(Long userId) {
       String token = generateSecureToken();
       redis.opsForValue().set(resetTokenKey(token), String.valueOf(userId), RESET_TOKEN_TTL);
       log.debug("Reset token generated | userId={}", userId);
       return token;
    }

    @Override
    public Long validateAndConsumeResetToken(String token) {
       String key = resetTokenKey(token);
       String userId = redis.opsForValue().get(key);
       if (userId == null) {
          throw new OtpException("Token is invalid or has expired.");
       }
       redis.delete(key);
       return Long.valueOf(userId);
    }

    private String randomOtp() {
       return String.format("%06d", new SecureRandom().nextInt(1_000_000));
    }

    private String generateSecureToken() {
       byte[] bytes = new byte[32];
       new SecureRandom().nextBytes(bytes);
       return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
