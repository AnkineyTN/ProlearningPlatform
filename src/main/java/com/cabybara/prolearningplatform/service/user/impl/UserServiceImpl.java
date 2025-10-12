package com.cabybara.prolearningplatform.service.impl;


import com.cabybara.prolearningplatform.dto.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.GoogleUserInfoDto;
import com.cabybara.prolearningplatform.dto.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.enums.UserEducation;
import com.cabybara.prolearningplatform.enums.UserHearAppFrom;
import com.cabybara.prolearningplatform.enums.UserLanguage;
import com.cabybara.prolearningplatform.exception.AuthException;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.Authority;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.UserService;
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
    //    private final EmailService emailService;
//    private final JwtService jwtService;
    private final UserMapper userMapper;

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
            throw new AuthException(HttpStatus.CONFLICT, "User has existed!");
        }

        User newUser = User.builder()
                .firstName(registerRequestDto.getFirstName())
                .lastName(registerRequestDto.getLastName())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .email(registerRequestDto.getEmail())
                .roles(new HashSet<>())
                .education(UserEducation.COLLEGE)
                .hearAppFrom(UserHearAppFrom.CLASSMATE)
                .language(UserLanguage.VI)
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
    public void updateUserPassword(String email, ChangePasswordRequestDto changePasswordRequestDto) {
        User existedUser = userRepository.findByEmail(email).orElseThrow(() -> new UsernameNotFoundException("User with username: " + email + " not found!"));

        String encodedNewPassword = passwordEncoder.encode(changePasswordRequestDto.getNewPassword());
        String encodedOldPassword = passwordEncoder.encode(changePasswordRequestDto.getOldPassword());

        if (existedUser.getPassword().equals(encodedOldPassword)) {
            throw new AuthException(HttpStatus.CONFLICT, "Old password is not match");
        }

        if (encodedNewPassword.equals(encodedOldPassword)) {
            throw new AuthException(HttpStatus.CONFLICT, "Password is equal to old password");
        }

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
                            .language(UserLanguage.VI)
                            .hearAppFrom(UserHearAppFrom.CLASSMATE)
                            .education(UserEducation.COLLEGE)
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
                            .language(UserLanguage.VI)
                            .hearAppFrom(UserHearAppFrom.CLASSMATE)
                            .education(UserEducation.COLLEGE)
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
}
