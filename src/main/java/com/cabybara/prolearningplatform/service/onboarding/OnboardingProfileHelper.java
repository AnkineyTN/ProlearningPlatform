package com.cabybara.prolearningplatform.service.onboarding;

import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingDataResponseDto;
import com.cabybara.prolearningplatform.model.User;

import java.time.Instant;

/**
 * Derives onboarding completion from persisted user profile fields (language, education, hearAppFrom).
 */
public final class OnboardingProfileHelper {

    private OnboardingProfileHelper() {
    }

    public static OnboardingDataResponseDto toDataOrNull(User user) {
        if (user.getLanguage() == null && user.getEducation() == null && user.getHearAppFrom() == null) {
            return null;
        }
        return OnboardingDataResponseDto.builder()
                .language(user.getLanguage())
                .education(user.getEducation())
                .hearAppFrom(user.getHearAppFrom())
                .accountType(user.getAccountType())
                .build();
    }

    public static Instant submittedAtOrNull(User user) {
        if (toDataOrNull(user) == null) {
            return null;
        }
        if (user.getUpdatedAt() == null) {
            return null;
        }
        return user.getUpdatedAt().toInstant();
    }

    /** Used after persisting profile changes (e.g. onboarding submit). */
    public static Instant lastProfileUpdateInstant(User user) {
        if (user.getUpdatedAt() != null) {
            return user.getUpdatedAt().toInstant();
        }
        return Instant.now();
    }
}
