package com.cabybara.prolearningplatform.service.user.impl;

import com.cabybara.prolearningplatform.dto.request.user.UserUpdatingRequestDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;

import com.cabybara.prolearningplatform.dto.request.user.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.helper.GoogleUserInfoDto;
import com.cabybara.prolearningplatform.dto.request.user.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.google.api.services.oauth2.model.Userinfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

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

        userMapper.updateUserFromDto(userUpdatingRequestDto, user);

        userRepository.save(user);

        return userMapper.toUserResponseDto(user);
    }

    @Override
    public void updateUserPassword(String email, ChangePasswordRequestDto changePasswordRequestDto) {
        User existedUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " not found!"));

        String encodedNewPassword = passwordEncoder.encode(changePasswordRequestDto.getNewPassword());

        if (!passwordEncoder.matches(changePasswordRequestDto.getOldPassword(), existedUser.getPassword())) {
            throw new AuthException(HttpStatus.CONFLICT, "Old password is not match");
        } new AuthException(HttpStatus.CONFLICT, "Password is equal to old password");

        existedUser.setPassword(encodedNewPassword);
        userRepository.save(existedUser);
    }

    @Override
    public void deleteUser(String email) {
        User existedUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " not found!"));

        userRepository.delete(existedUser);
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
}
