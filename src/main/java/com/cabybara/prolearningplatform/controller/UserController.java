package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.user.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.UserUpdatingRequestDto;

import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User")
@Validated
public class UserController {
    private final UserService userService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@AuthenticationPrincipal Jwt jwt) {
        UserResponseDto userResponseDto = userService.loadUserByEmail(jwt.getSubject());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", userResponseDto, null));
    }

    @Hidden
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updateUserPassword(@AuthenticationPrincipal Jwt jwt, @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        userService.updateUserPassword(jwt.getSubject(), changePasswordRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdatingRequestDto userUpdatingRequestDto) {
            Long userId = Long.parseLong(jwt.getClaims().get("id").toString());
            UserResponseDto userResponseDto = userService.updateUser(userId, userUpdatingRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", userResponseDto, null));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(jwt.getSubject());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }
}
