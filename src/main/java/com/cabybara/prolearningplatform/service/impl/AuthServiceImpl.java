package com.cabybara.prolearningplatform.service.impl;

import com.cabybara.prolearningplatform.dto.LoginResponseDto;
import com.cabybara.prolearningplatform.dto.RegisterRequestDto;
import com.cabybara.prolearningplatform.dto.RegisterResponseDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.mapper.UserMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.AuthService;
import com.cabybara.prolearningplatform.service.JwtService;
import com.cabybara.prolearningplatform.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;
    private final UserService userService;

    @Override
    public LoginResponseDto authenticateAndGenerateToken(String email, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        UserResponseDto userResponseDto = userMapper.toUserResponseDto((User) authentication.getPrincipal());
        String accessToken = jwtService.generateToken(authentication);

        return LoginResponseDto.builder()
                .userResponseDto(userResponseDto)
                .accessToken(accessToken)
                .build();
    }

    @Override
    public RegisterResponseDto registerUser(RegisterRequestDto registerRequestDto) throws Exception {
        Role role = Role.valueOf(registerRequestDto.getRole());
        UserResponseDto userResponseDto = userService.addUser(
                registerRequestDto,
                role
        );

        return RegisterResponseDto.builder()
                .id(userResponseDto.getId())
                .email(userResponseDto.getEmail())
                .firstName(userResponseDto.getFirstName())
                .lastName(userResponseDto.getLastName())
                .build();
    }

    @Override
    public RegisterResponseDto registerAdmin(RegisterRequestDto registerRequestDto) throws Exception {
        UserResponseDto userResponseDto = userService.addUser(
                registerRequestDto,
                Role.ROLE_ADMIN
        );

        return RegisterResponseDto.builder()
                .id(userResponseDto.getId())
                .email(userResponseDto.getEmail())
                .firstName(userResponseDto.getFirstName())
                .lastName(userResponseDto.getLastName())
                .build();
    }
}
