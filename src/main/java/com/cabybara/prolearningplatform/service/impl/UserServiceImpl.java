package com.cabybara.prolearningplatform.service.impl;


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
import lombok.RequiredArgsConstructor;
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
    public UserResponseDto addUser(RegisterRequestDto registerRequestDto, Role role) throws Exception {
        User existedUser = userRepository.findByEmail(registerRequestDto.getEmail()).orElseGet(() -> null);
        if (existedUser != null) {
            throw new AuthException("User has existed!");
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
}
