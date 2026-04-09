package com.cabybara.prolearningplatform.service.onboarding;

import com.cabybara.prolearningplatform.dto.request.onboarding.OnboardingSubmissionRequestDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingAnalyticsResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingSubmissionResponseDto;

public interface OnboardingSubmissionService {

    OnboardingSubmissionResponseDto submit(Long authenticatedUserId, OnboardingSubmissionRequestDto request);

    OnboardingAnalyticsResponseDto getAnalytics();
}
