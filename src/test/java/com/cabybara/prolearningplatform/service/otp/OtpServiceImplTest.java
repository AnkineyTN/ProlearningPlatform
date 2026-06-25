package com.cabybara.prolearningplatform.service.otp;

import com.cabybara.prolearningplatform.exception.OtpException;
import com.cabybara.prolearningplatform.service.otp.impl.OtpServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceImplTest {

    @Mock
    private StringRedisTemplate redis;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Test
    void generateVerifyOtpStoresOtpAttemptCounterAndResendLock() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.hasKey("otp:resend-lock:verify:1")).thenReturn(false);
        when(redis.opsForValue()).thenReturn(valueOperations);

        String otp = service.generateVerifyOtp(1L);

        assertNotNull(otp);
        assertEquals(6, otp.length());
        verify(valueOperations).set(eq("otp:verify:1"), eq(otp), eq(Duration.ofMinutes(10)));
        verify(valueOperations).set("otp:verify:1:attempts", "0", Duration.ofMinutes(15));
        verify(valueOperations).set("otp:resend-lock:verify:1", "1", Duration.ofSeconds(60));
    }

    @Test
    void generateResetOtpThrowsWhenResendLockExists() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.hasKey("otp:resend-lock:reset:2")).thenReturn(true);
        when(redis.getExpire("otp:resend-lock:reset:2")).thenReturn(45L);

        OtpException exception = assertThrows(OtpException.class, () -> service.generateResetOtp(2L));

        assertEquals("Please wait 45 seconds before resending.", exception.getMessage());
    }

    @Test
    void verifyVerifyOtpDeletesOtpAndAttemptKeyOnSuccess() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:verify:3")).thenReturn("123456");
        when(valueOperations.get("otp:verify:3:attempts")).thenReturn("0");

        service.verifyVerifyOtp(3L, "123456");

        verify(redis).delete("otp:verify:3");
        verify(redis).delete("otp:verify:3:attempts");
    }

    @Test
    void verifyResetOtpThrowsAndIncrementsAttemptsWhenOtpWrong() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:reset:4")).thenReturn("123456");
        when(valueOperations.get("otp:reset:4:attempts")).thenReturn("1");

        OtpException exception = assertThrows(OtpException.class, () -> service.verifyResetOtp(4L, "000000"));

        assertEquals("Incorrect OTP. 3 attempts remaining.", exception.getMessage());
        verify(valueOperations).increment("otp:reset:4:attempts");
    }

    @Test
    void verifyVerifyOtpThrowsWhenOtpExpired() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:verify:5")).thenReturn(null);

        OtpException exception = assertThrows(OtpException.class, () -> service.verifyVerifyOtp(5L, "123456"));

        assertEquals("OTP has expired or does not exist. Please request a new code.", exception.getMessage());
    }

    @Test
    void verifyVerifyOtpDeletesKeysWhenMaxAttemptsExceeded() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("otp:verify:6")).thenReturn("123456");
        when(valueOperations.get("otp:verify:6:attempts")).thenReturn("5");

        OtpException exception = assertThrows(OtpException.class, () -> service.verifyVerifyOtp(6L, "123456"));

        assertEquals("Exceeded maximum attempts of 5. Please request a new code.", exception.getMessage());
        verify(redis).delete("otp:verify:6");
        verify(redis).delete("otp:verify:6:attempts");
    }

    @Test
    void generateAndConsumeResetTokenStoresAndDeletesToken() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get(any())).thenReturn("7");

        String token = service.generateResetToken(7L);
        Long userId = service.validateAndConsumeResetToken(token);

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(7L, userId);
        verify(valueOperations).set(eq("reset-token:" + token), eq("7"), eq(Duration.ofMinutes(5)));
        verify(redis).delete("reset-token:" + token);
    }

    @Test
    void validateAndConsumeResetTokenThrowsWhenMissing() {
        OtpServiceImpl service = new OtpServiceImpl(redis);
        when(redis.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("reset-token:missing")).thenReturn(null);

        OtpException exception = assertThrows(OtpException.class, () -> service.validateAndConsumeResetToken("missing"));

        assertEquals("Token is invalid or has expired.", exception.getMessage());
        verify(redis, never()).delete("reset-token:missing");
    }
}
