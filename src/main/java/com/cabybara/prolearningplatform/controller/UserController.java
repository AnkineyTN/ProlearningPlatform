package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.UserResponseDto;
import com.cabybara.prolearningplatform.service.UserService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@AuthenticationPrincipal Jwt jwt) {
        UserResponseDto userResponseDto = userService.loadUserByEmail(jwt.getSubject());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", userResponseDto, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updateUserPassword(@AuthenticationPrincipal Jwt jwt, @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        userService.updateUserPassword(jwt.getSubject(), changePasswordRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
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
