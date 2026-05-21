package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.user.ChangePasswordRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.SetAvatarRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.UserUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserSearchResponse;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Page;
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
    private final AuthenticationContext authenticationContext;
    private final NotePermissionService notePermissionService;

    private static Long userIdFromJwt(Jwt jwt) {
        return Long.parseLong(jwt.getClaims().get("id").toString());
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUser(@AuthenticationPrincipal Jwt jwt) {
        UserResponseDto userResponseDto = userService.loadUserProfileById(userIdFromJwt(jwt));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", userResponseDto, null));
    }

    @Hidden
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/password")
    public ResponseEntity<ApiResponse<String>> updateUserPassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequestDto changePasswordRequestDto) {
        userService.updateUserPassword(userIdFromJwt(jwt), changePasswordRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Update current user profile", description = """
            Partial update. Omit fields you do not want to change.
            To change password, send both currentPassword and newPassword (for accounts that already have a password).
            Google/OAuth accounts without a password may set newPassword without currentPassword.""")
    @PatchMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdatingRequestDto userUpdatingRequestDto) {
        UserResponseDto userResponseDto = userService.updateUser(userIdFromJwt(jwt), userUpdatingRequestDto);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", userResponseDto, null));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<String>> deleteUser(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteUser(userIdFromJwt(jwt));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }

    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Set user avatar", description = "Pass the assetId returned from the upload callback to link the image as the user avatar.")
    @PostMapping("/me/avatar")
    public ResponseEntity<ApiResponse<UserResponseDto>> setAvatar(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody SetAvatarRequestDto request) {
        UserResponseDto updated = userService.setAvatar(userIdFromJwt(jwt), request.getAssetId());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Avatar updated", updated, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/me/resend-verify-otp")
    public ResponseEntity<ApiResponse<String>> resendVerifyOtp(@AuthenticationPrincipal Jwt jwt) {
        userService.resendVerifyOtp(userIdFromJwt(jwt));
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("OTP resent successfully", null, null));
    }

    @PreAuthorize("isAuthenticated() and @notePermissionService.hasAccess(@authenticationContext.getCurrentUserId(), #noteId)")
    @GetMapping("/notes/{noteId}/search")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
        @PathVariable Long noteId,
        @RequestParam String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserSearchResponse> searchResults = userService.searchNoteUsers(keyword, page, size, noteId);

        PaginationResponseDto paginationResponseDto = PaginationResponseDto.builder()
                .currentPage(searchResults.getNumber())
                .totalPages(searchResults.getTotalPages())
                .totalItems(searchResults.getTotalElements())
                .pageSize(searchResults.getSize())
                .build();

        return ResponseEntity.status(HttpStatus.OK).body(
            ResponseUtil.success(
                "Search completed",
                searchResults.getContent(),
                paginationResponseDto
            )
        );
    }
    
}
