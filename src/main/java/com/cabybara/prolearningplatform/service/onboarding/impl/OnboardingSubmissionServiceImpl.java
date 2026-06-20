package com.cabybara.prolearningplatform.service.onboarding.impl;

import com.cabybara.prolearningplatform.dto.helper.LabelCountProjection;
import com.cabybara.prolearningplatform.dto.request.onboarding.OnboardingSubmissionRequestDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.ChartBucketResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingAnalyticsResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.OnboardingSubmissionResponseDto;
import com.cabybara.prolearningplatform.dto.response.onboarding.PremiumAccountStatsResponseDto;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingProfileHelper;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingSubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

        OnboardingSubmissionRequestDto.DataPayload d = request.getData();
        user.setLanguage(d.getLanguage());
        user.setEducation(d.getEducation());
        user.setHearAppFrom(d.getHearAppFrom());
        user.setAccountType(d.getAccountType());

        User saved = userRepository.save(user);
        return toResponse(saved);
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

    private List<ChartBucketResponseDto> toBuckets(List<LabelCountProjection> rows, long denominator) {
        List<ChartBucketResponseDto> out = new ArrayList<>();
        if (rows == null || denominator <= 0) {
            return out;
        }
        for (LabelCountProjection row : rows) {
            if (row == null) {
                continue;
            }
            String label = row.getLabel() != null ? row.getLabel() : "UNSET";
            long count = row.getCount();
            out.add(ChartBucketResponseDto.builder()
                    .label(label)
                    .count(count)
                    .percent(percent(count, denominator))
                    .build());
        }
        return out;
    }

    private PremiumAccountStatsResponseDto toPremiumStats(List<LabelCountProjection> rows, long denominator) {
        long proCount = 0;
        long freeCount = 0;
        if (rows != null) {
            for (LabelCountProjection row : rows) {
                if (row == null) {
                    continue;
                }
                String type = row.getLabel() != null ? row.getLabel() : "";
                long c = row.getCount();
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
        return OnboardingSubmissionResponseDto.builder()
                .id(user.getId())
                .submittedAt(OnboardingProfileHelper.lastProfileUpdateInstant(user))
                .userId(user.getId())
                .data(OnboardingProfileHelper.toDataOrNull(user))
                .build();
    }
}
