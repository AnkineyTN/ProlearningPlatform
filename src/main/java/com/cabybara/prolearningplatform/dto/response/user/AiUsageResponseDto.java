package com.cabybara.prolearningplatform.dto.response.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AiUsageResponseDto {
    private String tier; // "FREE" or "PRO"
    private boolean byokActive; // true if using their own API key
    private UsageDetail generation;
    private UsageDetail interactive;

    @Data
    @Builder
    public static class UsageDetail {
        private int limit;
        private int used;
        private int remaining;
        private long resetTimeSeconds; // TTL remaining in seconds
    }
}
