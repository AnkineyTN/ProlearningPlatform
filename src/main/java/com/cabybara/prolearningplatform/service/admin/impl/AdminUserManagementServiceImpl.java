package com.cabybara.prolearningplatform.service.admin.impl;

import com.cabybara.prolearningplatform.dto.request.admin.AdminUserUpdateRequestDto;
import com.cabybara.prolearningplatform.dto.response.admin.AdminUserListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.admin.AdminUserManagementService;
import com.cabybara.prolearningplatform.service.onboarding.OnboardingProfileHelper;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AdminUserManagementServiceImpl implements AdminUserManagementService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional(readOnly = true)
    public Page<AdminUserListItemResponseDto> listUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::toListItem);
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

    private static boolean hasAdminRole(User user) {
        if (user.getRoles() == null) {
            return false;
        }
        return user.getRoles().stream().anyMatch(a -> a.getAuthority() == Role.ROLE_ADMIN);
    }
}
