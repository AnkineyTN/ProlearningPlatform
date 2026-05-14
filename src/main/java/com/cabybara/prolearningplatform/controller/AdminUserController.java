package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.admin.AdminBlockUserRequestDto;
import com.cabybara.prolearningplatform.dto.request.admin.AdminUserUpdateRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.admin.AdminUserListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.user.UserResponseDto;
import com.cabybara.prolearningplatform.service.admin.AdminUserManagementService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Tag(name = "Admin — Users", description = "User directory management for administrators")
@SecurityRequirement(name = "bearerAuth")
@Validated
public class AdminUserController {

    private final AdminUserManagementService adminUserManagementService;

    @Operation(
            summary = "List users",
            description = """
                    Paginated user directory for admins. Each row includes `user` (account and roles) and optional \
                    `onboarding` / `onboardingSubmittedAt` when the user has completed onboarding (language, education, \
                    and hear-app-from are present). If not onboarded, `onboarding` and `onboardingSubmittedAt` are null— \
                    clients may show empty cells or a placeholder such as "--".""")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    @ValidateSort(allowedFields = {"id", "createdAt", "updatedAt", "email", "firstName", "lastName", "accountType"})
    public ResponseEntity<ApiResponse<Object>> listUsers(
            @ParameterObject @PageableDefault(page = 0, size = 20, sort = "id", direction = Sort.Direction.DESC)
            Pageable pageable) {
        Page<AdminUserListItemResponseDto> page = adminUserManagementService.listUsers(pageable);
        PaginationResponseDto meta = PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), meta));
    }

    @Operation(
            summary = "Update a user",
            description = """
                    Partial update of display name and subscription tier. Send only the fields to change.
                    Set accountType to PRO or FREE to upgrade or downgrade the user.""")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{userId}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @Parameter(description = "Target user identifier") @PathVariable Long userId,
            @Valid @RequestBody AdminUserUpdateRequestDto request) {
        UserResponseDto updated = adminUserManagementService.updateUser(userId, request);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", updated, null));
    }

    @Operation(
            summary = "Delete a user",
            description = """
                    Permanently removes the user and dependent data according to database cascades.
                    Cannot delete your own account or another administrator.""")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "OK"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/{userId}")
    public ResponseEntity<ApiResponse<String>> deleteUser(
            @Parameter(description = "User to delete") @PathVariable Long userId) {
        adminUserManagementService.deleteUser(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", null, null));
    }

    @Operation(summary = "Block a user", description = "Suspends the account and sends a notification to the user.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{userId}/block")
    public ResponseEntity<ApiResponse<UserResponseDto>> blockUser(
            @PathVariable Long userId,
            @Valid @RequestBody AdminBlockUserRequestDto request) {
        UserResponseDto updated = adminUserManagementService.blockUser(userId, request.getReason());
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("User blocked", updated, null));
    }

    @Operation(summary = "Unblock a user", description = "Reinstates the account and notifies the user.")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/{userId}/unblock")
    public ResponseEntity<ApiResponse<UserResponseDto>> unblockUser(
            @PathVariable Long userId) {
        UserResponseDto updated = adminUserManagementService.unblockUser(userId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("User unblocked", updated, null));
    }
}
