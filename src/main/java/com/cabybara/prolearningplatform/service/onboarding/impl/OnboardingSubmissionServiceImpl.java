package com.cabybara.prolearningplatform.service.onboarding.impl;

import com.cabybara.prolearningplatform.dto.request.onboarding.OnboardingSubmissionRequestDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.ChartBucketResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingAnalyticsResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingDataResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingSubmissionResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.PremiumAccountStatsResponseDto;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OnboardingSubmissionServiceImpl implements OnboardingSubmissionService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OnboardingSubmissionResponseDto submit(Long authenticatedUserId, OnboardingSubmissionRequestDto request) {
        if (!authenticatedUserId.equals(request.getUserId())) {
            throw new BadRequestException("userId must match the authenticated user");
        }
        User user = userRepository.findById(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + authenticatedUserId + " not found"));

        if (!user.getEmail().equalsIgnoreCase(request.getEmail().trim())) {
            throw new BadRequestException("email must match the authenticated user's email");
        }

        OnboardingSubmissionRequestDto.DataPayload d = request.getData();
        user.setLanguage(d.getLanguage());
        user.setEducation(d.getEducation());
        user.setHearAppFrom(d.getHearAppFrom());
        user.setAccountType(d.getAccountType());
        applyDisplayName(user, request.getDisplayName());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OnboardingSubmissionResponseDto> listAll(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public OnboardingAnalyticsResponseDto getAnalytics() {
        long total = userRepository.count();
        long denom = total;
        List<ChartBucketResponseDto> education = toBuckets(userRepository.aggregateEducation(), denom);
        List<ChartBucketResponseDto> hearAppFrom = toBuckets(userRepository.aggregateHearAppFrom(), denom);
        PremiumAccountStatsResponseDto premium = toPremiumStats(userRepository.aggregateAccountType(), denom);
        return OnboardingAnalyticsResponseDto.builder()
                .totalRegisteredUsers(total)
                .education(education)
                .hearAppFrom(hearAppFrom)
                .premium(premium)
                .build();
    }

    private List<ChartBucketResponseDto> toBuckets(List<Object[]> rows, long denominator) {
        List<ChartBucketResponseDto> out = new ArrayList<>();
        if (rows == null || denominator <= 0) {
            return out;
        }
        for (Object[] row : rows) {
            if (row == null || row.length < 2) {
                continue;
            }
            String label = row[0] != null ? row[0].toString() : "UNSET";
            long count = ((Number) row[1]).longValue();
            out.add(ChartBucketResponseDto.builder()
                    .label(label)
                    .count(count)
                    .percent(percent(count, denominator))
                    .build());
        }
        return out;
    }

    private PremiumAccountStatsResponseDto toPremiumStats(List<Object[]> rows, long denominator) {
        long proCount = 0;
        long freeCount = 0;
        if (rows != null) {
            for (Object[] row : rows) {
                if (row == null || row.length < 2) {
                    continue;
                }
                String type = row[0] != null ? row[0].toString() : "";
                long c = ((Number) row[1]).longValue();
                if ("PRO".equalsIgnoreCase(type)) {
                    proCount += c;
                } else {
                    freeCount += c;
                }
            }
        }
        double pct = denominator > 0 ? percent(proCount, denominator) : 0.0;
        return PremiumAccountStatsResponseDto.builder()
                .proCount(proCount)
                .freeCount(freeCount)
                .proPercent(pct)
                .build();
    }

    private static double percent(long part, long total) {
        if (total <= 0) {
            return 0.0;
        }
        return BigDecimal.valueOf(part)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private OnboardingSubmissionResponseDto toResponse(User user) {
        Instant submittedAt = user.getUpdatedAt() != null
                ? user.getUpdatedAt().toInstant()
                : Instant.now();
        return OnboardingSubmissionResponseDto.builder()
                .id(user.getId())
                .submittedAt(submittedAt)
                .userId(user.getId())
                .email(user.getEmail())
                .displayName(displayNameOf(user))
                .data(toDataOrNull(user))
                .build();
    }

    private static OnboardingDataResponseDto toDataOrNull(User user) {
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

    private static void applyDisplayName(User user, String displayName) {
        if (displayName == null || displayName.isBlank()) {
            return;
        }
        String[] parts = displayName.trim().split("\\s+", 2);
        user.setFirstName(parts[0]);
        user.setLastName(parts.length > 1 ? parts[1] : parts[0]);
    }

    private static String displayNameOf(User user) {
        String fn = user.getFirstName() != null ? user.getFirstName() : "";
        String ln = user.getLastName() != null ? user.getLastName() : "";
        String joined = (fn + " " + ln).trim();
        return joined.isEmpty() ? null : joined;
    }
}
