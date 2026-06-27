package com.cabybara.prolearningplatform.service.permission.impl;

import com.cabybara.prolearningplatform.dto.response.user.AiUsageResponseDto;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.cabybara.prolearningplatform.service.permission.AccountPermissionService;
import com.cabybara.prolearningplatform.service.permission.AiUsageService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiUsageServiceImpl implements AiUsageService {

    private final RedisService redisService;
    private final AccountPermissionService accountPermissionService;
    private final UserLlmConfigService userLlmConfigService;
    private final Environment env;

    @Override
    public AiUsageResponseDto getAiUsage(Long userId) {
        boolean isPro = accountPermissionService.isPro(userId);
        String tier = isPro ? "PRO" : "FREE";
        boolean byokActive = userLlmConfigService.getDecryptedConfig(userId) != null;

        return AiUsageResponseDto.builder()
                .tier(tier)
                .byokActive(byokActive)
                .generation(getUsageDetail(userId, "AI_GENERATION", isPro, byokActive))
                .interactive(getUsageDetail(userId, "AI_INTERACTIVE", isPro, byokActive))
                .build();
    }

    private AiUsageResponseDto.UsageDetail getUsageDetail(Long userId, String type, boolean isPro, boolean byokActive) {
        String typeSlug = type.toLowerCase().replace("_", "-");
        String limitProp = "app.rate-limit." + typeSlug + "." + (isPro ? "pro-limit" : "free-limit");
        String periodProp = "app.rate-limit." + typeSlug + ".period-seconds";

        Integer limit = env.getProperty(limitProp, Integer.class);
        Long period = env.getProperty(periodProp, Long.class);

        int finalLimit = (limit != null) ? limit : (isPro ? 100 : (type.equals("AI_GENERATION") ? 5 : 20));

        if (byokActive) {
            return AiUsageResponseDto.UsageDetail.builder()
                    .limit(-1)
                    .used(0)
                    .remaining(-1)
                    .resetTimeSeconds(0)
                    .build();
        }

        String key = "ratelimit:" + userId + ":" + type;
        Object val = redisService.get(key);
        int used = 0;

        if (val != null) {
            if (val instanceof Integer) {
                used = (Integer) val;
            } else if (val instanceof Long) {
                used = ((Long) val).intValue();
            } else if (val instanceof String) {
                try {
                    used = Integer.parseInt((String) val);
                } catch (NumberFormatException e) {
                    log.error("Failed to parse rate limit count from Redis value: {}", val);
                }
            }
        }

        int remaining = Math.max(0, finalLimit - used);

        Long ttl = redisService.getTTL(key);
        long resetTimeSeconds = (ttl != null && ttl > 0) ? ttl : 0;

        return AiUsageResponseDto.UsageDetail.builder()
                .limit(finalLimit)
                .used(used)
                .remaining(remaining)
                .resetTimeSeconds(resetTimeSeconds)
                .build();
    }
}
