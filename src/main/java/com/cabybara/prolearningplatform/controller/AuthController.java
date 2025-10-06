package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.LoginUserDto;
import com.cabybara.prolearningplatform.dto.RegisterUserDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.enums.Role;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.service.JwtService;
import com.cabybara.prolearningplatform.service.UserService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerNormalUser(@Valid @RequestBody RegisterUserDto registerUserDto) throws Exception {
        UserResponseDto userResponseDto = userService.addUser(registerUserDto, Role.valueOf(registerUserDto.getRole()));
        ApiResponse<UserResponseDto> response = ResponseUtil.success("Registration successfully", userResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerAdminUser(@Valid @RequestBody RegisterUserDto registerUserDto) throws Exception {
        UserResponseDto userResponseDto = userService.addUser(registerUserDto, Role.ROLE_ADMIN);
        ApiResponse<UserResponseDto> response = ResponseUtil.success("Registration successfully", userResponseDto, null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<User>> login(@Valid @RequestBody LoginUserDto loginUserDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginUserDto.getEmail(), loginUserDto.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String jwt = jwtService.generateToken(authentication);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Login successfully", user, jwt));

    }

}
