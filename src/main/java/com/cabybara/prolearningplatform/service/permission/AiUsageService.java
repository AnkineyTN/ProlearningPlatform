package com.cabybara.prolearningplatform.service.permission;

import com.cabybara.prolearningplatform.dto.response.user.AiUsageResponseDto;

public interface AiUsageService {
    AiUsageResponseDto getAiUsage(Long userId);
}
