package com.cabybara.prolearningplatform.service.appeal.impl;

import com.cabybara.prolearningplatform.dto.request.admin.AdminAppealReviewDto;
import com.cabybara.prolearningplatform.dto.response.appeal.AppealResponseDto;
import com.cabybara.prolearningplatform.enums.AppealStatus;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.UserAppeal;
import com.cabybara.prolearningplatform.repository.UserAppealRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.appeal.AppealService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AppealServiceImpl implements AppealService {

    private final UserAppealRepository appealRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public AppealResponseDto submitPublicAppeal(String email, String reason) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with this email"));
        if (!user.isBlocked()) {
            throw new BadRequestException("Account is not suspended");
        }
        if (appealRepository.existsByUserIdAndStatus(user.getId(), AppealStatus.PENDING)) {
            throw new BadRequestException("A pending appeal already exists for this account");
        }
        UserAppeal appeal = UserAppeal.builder()
                .user(user)
                .email(email)
                .reason(reason)
                .status(AppealStatus.PENDING)
                .build();
        return toDto(appealRepository.save(appeal));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AppealResponseDto> listAppeals(AppealStatus status, Pageable pageable) {
        if (status != null) {
            return appealRepository.findByStatus(status, pageable).map(this::toDto);
        }
        return appealRepository.findAll(pageable).map(this::toDto);
    }

    @Override
    @Transactional
    public AppealResponseDto reviewAppeal(Long appealId, AdminAppealReviewDto dto) {
        UserAppeal appeal = appealRepository.findById(appealId)
                .orElseThrow(() -> new ResourceNotFoundException("Appeal not found"));
        if (appeal.getStatus() != AppealStatus.PENDING) {
            throw new BadRequestException("Appeal has already been reviewed");
        }
        appeal.setStatus(dto.getStatus());
        appeal.setAdminNote(dto.getAdminNote());
        appeal.setResolvedAt(OffsetDateTime.now());
        if (dto.getStatus() == AppealStatus.ACCEPTED) {
            User user = appeal.getUser();
            user.setBlocked(false);
            user.setBlockedAt(null);
            userRepository.save(user);
        }
        return toDto(appealRepository.save(appeal));
    }

    private AppealResponseDto toDto(UserAppeal appeal) {
        return AppealResponseDto.builder()
                .id(appeal.getId())
                .userId(appeal.getUser() != null ? appeal.getUser().getId() : null)
                .email(appeal.getEmail())
                .reason(appeal.getReason())
                .status(appeal.getStatus())
                .adminNote(appeal.getAdminNote())
                .createdAt(appeal.getCreatedAt())
                .resolvedAt(appeal.getResolvedAt())
                .build();
    }
}
