package com.cabybara.prolearningplatform.service.permission.aspect;

import com.cabybara.prolearningplatform.exception.RateLimitExceededException;
import com.cabybara.prolearningplatform.service.permission.AccountPermissionService;
import com.cabybara.prolearningplatform.service.permission.annotation.AiRateLimit;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class AiRateLimitAspect {

    private final RedisService redisService;
    private final AuthenticationContext authenticationContext;
    private final AccountPermissionService accountPermissionService;
    private final Environment env;

    @Before("@annotation(aiRateLimit)")
    public void checkRateLimit(JoinPoint joinPoint, AiRateLimit aiRateLimit) {
        Long userId;
        try {
            userId = authenticationContext.getCurrentUserId();
        } catch (Exception e) {
            log.warn("Rate limit check bypassed: User is not authenticated or context missing. Method: {}", joinPoint.getSignature().toShortString());
            return;
        }

        String type = aiRateLimit.type();
        boolean isPro = accountPermissionService.isPro(userId);

        // Map type dynamically to configuration properties:
        // E.g. AI_GENERATION -> app.rate-limit.ai-generation.free-limit / pro-limit
        String typeSlug = type.toLowerCase().replace("_", "-");
        String limitProp = "app.rate-limit." + typeSlug + "." + (isPro ? "pro-limit" : "free-limit");
        String periodProp = "app.rate-limit." + typeSlug + ".period-seconds";

        Integer limit = env.getProperty(limitProp, Integer.class);
        Long period = env.getProperty(periodProp, Long.class);

        // Fallbacks if not configured
        int finalLimit = (limit != null) ? limit : (isPro ? 100 : 5);
        long finalPeriod = (period != null) ? period : 86400; // 24 hours

        String key = "ratelimit:" + userId + ":" + type;
        Object val = redisService.get(key);
        int count = 0;

        if (val != null) {
            if (val instanceof Integer) {
                count = (Integer) val;
            } else if (val instanceof Long) {
                count = ((Long) val).intValue();
            } else if (val instanceof String) {
                try {
                    count = Integer.parseInt((String) val);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse rate limit count from Redis value: {}", val);
                }
            }
        }

        if (count >= finalLimit) {
            Long ttl = redisService.getTTL(key);
            long remaining = (ttl != null && ttl > 0) ? ttl : finalPeriod;
            throw new RateLimitExceededException(
                    String.format("You have exceeded your AI API usage limit (%d requests per %s). Please try again in %s.",
                            finalLimit, formatPeriod(finalPeriod), formatDuration(remaining))
            );
        }

        if (val == null) {
            redisService.set(key, 1, finalPeriod);
            log.debug("[AiRateLimit] First request initialized for user {}, type {}, period {}s", userId, type, finalPeriod);
        } else {
            Long ttl = redisService.getTTL(key);
            long remainingTtl = (ttl != null && ttl > 0) ? ttl : finalPeriod;
            redisService.set(key, count + 1, remainingTtl);
            log.debug("[AiRateLimit] Request incremented for user {}, type {}, count {}/{}", userId, type, count + 1, finalLimit);
        }
    }

    private String formatPeriod(long seconds) {
        if (seconds == 86400) return "day";
        if (seconds == 3600) return "hour";
        if (seconds == 60) return "minute";
        return seconds + " seconds";
    }

    private String formatDuration(long seconds) {
        if (seconds <= 0) return "a moment";
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        if (hours > 0) {
            return String.format("%d hours %d minutes", hours, minutes);
        } else if (minutes > 0) {
            return String.format("%d minutes %d seconds", minutes, secs);
        } else {
            return String.format("%d seconds", secs);
        }
    }
}
