package com.cabybara.prolearningplatform.service.onboarding;

import com.cabybara.prolearningplatform.dto.request.onboarding.OnboardingSubmissionRequestDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingAnalyticsResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingSubmissionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OnboardingSubmissionService {

    OnboardingSubmissionResponseDto submit(Long authenticatedUserId, OnboardingSubmissionRequestDto request);

    Page<OnboardingSubmissionResponseDto> listAll(Pageable pageable);

    OnboardingAnalyticsResponseDto getAnalytics();
}
