package com.cabybara.prolearningplatform.service.admin.impl;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.request.admin.AdminUserUpdateRequestDto;
import com.cabybara.prolearningplatform.dto.response.admin.AdminUserListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.admin.AdminUserStatsResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.repository.PomodoroSessionRepository;
import com.cabybara.prolearningplatform.service.admin.AdminUserManagementService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingProfileHelper;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    public static final String BLOCKED_USER_KEY_PREFIX = "user:blocked:";
    private static final long BLOCK_REDIS_TTL_SECONDS = 24 * 60 * 60; // 24 h covers any active access token

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationContext authenticationContext;
    private final RedisService redisService;
    private final NotificationDispatcher notificationDispatcher;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final PomodoroSessionRepository pomodoroSessionRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListItemResponseDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListItemResponseDto> listUsers(String keyword, String accountType, Pageable pageable) {
        String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();
        AccountType at = null;
        if (accountType != null && !accountType.isBlank()) {
            try {
                at = AccountType.valueOf(accountType.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Invalid accountType: " + accountType);
            }
        }
        return userRepository.searchForAdmin(kw, at, pageable).map(this::toListItem);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListItemResponseDto> listBlockedUsers(Pageable pageable) {
        return userRepository.findByIsBlocked(true, pageable).map(this::toListItem);
    }

    private AdminUserListItemResponseDto toListItem(User user) {
        return AdminUserListItemResponseDto.builder()
                .user(userMapper.toUserResponseDto(user))
                .onboarding(OnboardingProfileHelper.toDataOrNull(user))
                .onboardingSubmittedAt(OnboardingProfileHelper.submittedAtOrNull(user))
                .build();
    }

    @Override
    @Transactional
    public UserResponseDto updateUser(Long userId, AdminUserUpdateRequestDto request) {
        if (!hasAnyField(request)) {
            throw new BadRequestException("At least one field must be provided to update");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));

        if (request.getFirstName() != null) {
            if (!StringUtils.hasText(request.getFirstName())) {
                throw new BadRequestException("firstName must not be blank when provided");
            }
            user.setFirstName(request.getFirstName().trim());
        }
        if (request.getLastName() != null) {
            if (!StringUtils.hasText(request.getLastName())) {
                throw new BadRequestException("lastName must not be blank when provided");
            }
            user.setLastName(request.getLastName().trim());
        }
        if (request.getAccountType() != null) {
            user.setAccountType(request.getAccountType());
        }

        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        Long currentAdminId = authenticationContext.getCurrentUserId();
        if (userId.equals(currentAdminId)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Administrators cannot delete their own account");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        if (hasAdminRole(user)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Cannot delete a user with administrative privileges");
        }
        userRepository.delete(user);
    }

    private static boolean hasAnyField(AdminUserUpdateRequestDto request) {
        return request.getFirstName() != null
                || request.getLastName() != null
                || request.getAccountType() != null;
    }

    @Override
    @Transactional
    public UserResponseDto blockUser(Long userId, String reason) {
        Long currentAdminId = authenticationContext.getCurrentUserId();
        if (userId.equals(currentAdminId)) {
            throw new BadRequestException("Cannot block your own account");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        if (hasAdminRole(user)) {
            throw new AuthException(HttpStatus.FORBIDDEN, "Cannot block an administrator");
        }
        user.setBlocked(true);
        user.setBlockReason(reason);
        user.setBlockedAt(OffsetDateTime.now());
        userRepository.save(user);

        redisService.set(BLOCKED_USER_KEY_PREFIX + userId, "1", BLOCK_REDIS_TTL_SECONDS);

        notificationDispatcher.dispatchToUser(
                userId,
                NotificationType.ACCOUNT_BLOCKED.getDefaultTitle(),
                "Your account has been suspended. Reason: " + reason,
                NotificationType.ACCOUNT_BLOCKED);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional
    public UserResponseDto unblockUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        if (!user.isBlocked()) {
            throw new BadRequestException("User is not blocked");
        }
        user.setBlocked(false);
        user.setBlockReason(null);
        user.setBlockedAt(null);
        userRepository.save(user);

        redisService.delete(BLOCKED_USER_KEY_PREFIX + userId);

        notificationDispatcher.dispatchToUser(
                userId,
                NotificationType.ACCOUNT_UNBLOCKED.getDefaultTitle(),
                "Your account suspension has been lifted. You may now log in again.",
                NotificationType.ACCOUNT_UNBLOCKED);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AdminUserStatsResponseDto getUserStats(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
        return AdminUserStatsResponseDto.builder()
                .noteCount(noteRepository.countByUserId(userId))
                .flashcardCount(flashcardRepository.countByUserId(userId))
                .examCount(examRepository.countByCreatedBy(userId))
                .pomodoroSessionCount(pomodoroSessionRepository.countByUser_Id(userId))
                .registeredAt(user.getCreatedAt())
                .build();
    }

    private static boolean hasAdminRole(User user) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().anyMatch(a -> a.getAuthority() == Role.ROLE_ADMIN);
    }
}
