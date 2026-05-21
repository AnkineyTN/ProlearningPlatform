package com.cabybara.prolearningplatform.service.user.impl;

import com.cabybara.prolearningplatform.dto.request.user.UserUpdatingRequestDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.repository.AssetRepository;

import com.cabybara.prolearningplatform.dto.request.user.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.helper.GoogleUserInfoDto;
import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserSearchResponse;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.exception.BadRequestException;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.otp.OtpService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.google.api.services.oauth2.model.Userinfo;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final EmailService emailService;
    private final OtpService otpService;
    private final AssetRepository assetRepository;

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id " + userId + " not found"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByEmail(username);
        return user.orElseThrow(() -> new UsernameNotFoundException("User with username: " + username + " not found!"));
    }

    @Override
    public UserResponseDto loadUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return userMapper.toUserResponseDto(user.orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " not found!")));
    }

    @Override
    public UserResponseDto loadUserProfileById(Long userId) {
        return userMapper.toUserResponseDto(getUserById(userId));
    }

    @Override
    public UserResponseDto addUser(RegisterRequestDto registerRequestDto, Role role) throws Exception {
        User existedUser = userRepository.findByEmail(registerRequestDto.getEmail()).orElseGet(() -> null);
        if (existedUser != null) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "User has existed!");
        }

        User newUser = User.builder()
                .firstName(registerRequestDto.getFirstName())
                .lastName(registerRequestDto.getLastName())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .email(registerRequestDto.getEmail())
                .roles(new HashSet<>())
                .build();

        Authority defaultAuthority = Authority.builder()
                .user(newUser)
                .authority(role)
                .build();

        newUser.getRoles().add(defaultAuthority);

        userRepository.save(newUser);
        return userMapper.toUserResponseDto(newUser);
    }

    @Override
    public UserResponseDto updateUser(Long userId, UserUpdatingRequestDto userUpdatingRequestDto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found!"));

        if (StringUtils.hasText(userUpdatingRequestDto.getEmail())) {
            String trimmed = userUpdatingRequestDto.getEmail().trim();
            userRepository.findByEmail(trimmed)
                    .filter(u -> !u.getId().equals(userId))
                    .ifPresent(u -> {
                        throw new AuthException(HttpStatus.CONFLICT, "Email is already in use");
                    });
            userUpdatingRequestDto.setEmail(trimmed);
        }

        userMapper.updateUserFromDto(userUpdatingRequestDto, user);
        applyPasswordChangeFromProfile(user, userUpdatingRequestDto.getCurrentPassword(), userUpdatingRequestDto.getNewPassword());

        userRepository.save(user);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public void updateUserPassword(Long userId, ChangePasswordRequestDto changePasswordRequestDto) {
        User existedUser = getUserById(userId);
        applyPasswordChangeFromProfile(
                existedUser,
                changePasswordRequestDto.getOldPassword(),
                changePasswordRequestDto.getNewPassword());
        userRepository.save(existedUser);
    }

    @Override
    public void deleteUser(Long userId) {
        User existedUser = getUserById(userId);
        userRepository.delete(existedUser);
    }

    private void applyPasswordChangeFromProfile(User user, String currentPassword, String newPassword) {
        if (!StringUtils.hasText(newPassword)) {
            if (StringUtils.hasText(currentPassword)) {
                throw new BadRequestException("newPassword is required when currentPassword is provided");
            }
            return;
        }
        if (user.getPassword() != null) {
            if (!StringUtils.hasText(currentPassword)) {
                throw new BadRequestException("currentPassword is required to change password");
            }
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new AuthException(HttpStatus.CONFLICT, "Current password does not match");
            }
            if (passwordEncoder.matches(newPassword, user.getPassword())) {
                throw new AuthException(HttpStatus.CONFLICT, "New password must differ from current password");
            }
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    @Override
    public User findOrCreateFromGoogle(Userinfo userInfo) {

        return userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(userInfo.getEmail())
                            .firstName(userInfo.getFamilyName())
                            .lastName(userInfo.getGivenName())
                            .roles(new HashSet<>())
                            .build();

                    Authority defaultAuthority = Authority.builder()
                            .user(newUser)
                            .authority(Role.ROLE_USER)
                            .build();

                    newUser.getRoles().add(defaultAuthority);

                    return userRepository.save(newUser);
                });
    }

    @Override
    public User findOrCreateFromGoogle(GoogleUserInfoDto googleUserInfoDto) {

        return userRepository.findByEmail(googleUserInfoDto.getEmail())
                .orElseGet(() -> {
                    User newUser = User.builder()
                            .email(googleUserInfoDto.getEmail())
                            .firstName(googleUserInfoDto.getFamilyName())
                            .lastName(googleUserInfoDto.getGivenName())
                            .roles(new HashSet<>())
                            .build();

                    Authority defaultAuthority = Authority.builder()
                            .user(newUser)
                            .authority(Role.ROLE_USER)
                            .build();

                    newUser.getRoles().add(defaultAuthority);

                    return userRepository.save(newUser);
                });
    }

    @Override
    public void verifyEmail(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found!"));

        if (user.isEmailVerified()) {
            throw new AuthException(HttpStatus.BAD_REQUEST, "Email has been verified!");
        }

        user.setEmailVerified(true);
        userRepository.save(user);
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User with email: " + email + " not found!"));
    }

    @Override
    public void resetPassword(Long userId, String newPassword) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found!"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public UserResponseDto setAvatar(Long userId, Long assetId) {
        User user = getUserById(userId);
        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found"));
        if (!asset.getUser().getId().equals(userId)) {
            throw new com.cabybara.prolearningplatform.exception.BadRequestException("Asset does not belong to this user");
        }
        user.setAvatarUrl(asset.getUrl());
        userRepository.save(user);
        return userMapper.toUserResponseDto(user);
    }

    @Override
    public void resendVerifyOtp(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + " not found!"));

        if (user.isEmailVerified()) {
            return;
        }

        String otp = otpService.generateVerifyOtp(userId);

        emailService.sendVerifyOtp(user.getEmail(), user.getUsername(), otp);
    }

    @Override
    public Page<UserSearchResponse> searchNoteUsers(String keyword, int page, int size, Long noteId) {
        if (!StringUtils.hasText(keyword)) {
            return Page.empty();
        }

        return userRepository.searchByNameOrEmail(keyword, noteId, PageRequest.of(page, size))
            .map(UserSearchResponse::from);
    }
}
