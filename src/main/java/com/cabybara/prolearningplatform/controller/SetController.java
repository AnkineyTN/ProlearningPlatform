package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.SetUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.cabybara.prolearningplatform.dto.request.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.response.SetResponseDto;
import com.cabybara.prolearningplatform.service.set.SetService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sets")
@Tag(name = "Set APIs")
@RequiredArgsConstructor
public class SetController {
    private final SetService setService;

    @Operation(
            summary = "Get all sets of the current user",
            description = "Retrieve a paginated list of all sets created by the authenticated user."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Object>> getSet(
            @AuthenticationPrincipal Jwt jwt,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {

        Page<SetResponseDto> allSetResponseDtos = setService.getAllSet((Long) jwt.getClaims().get("id"), pageable);
        PaginationResponseDto paginationResponseDto = PaginationResponseDto.builder()
                .currentPage(allSetResponseDtos.getNumber())
                .totalPages(allSetResponseDtos.getTotalPages())
                .totalItems(allSetResponseDtos.getTotalElements())
                .pageSize(allSetResponseDtos.getSize())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(
                        "Successfully get all set",
                        allSetResponseDtos.getContent(),
                        paginationResponseDto
                ));
    }

    @Operation(
            summary = "Get detail a set of the current user"
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("{setId}")
    public ResponseEntity<ApiResponse<SetResponseDto>> getSet(@PathVariable Long setId) {
        SetResponseDto setResponseDto = setService.getSet(setId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", setResponseDto, null));
    }

    @Operation(
            summary = "Create a new set",
            description = "Create a new set for the authenticated user using the provided data."
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public ResponseEntity<ApiResponse<SetResponseDto>> createSet(
            @Valid @RequestBody SetCreationRequestDto setCreationRequestDto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        SetResponseDto setResponseDto = setService.createSet((Long) jwt.getClaims().get("id"), setCreationRequestDto);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create set successfully", setResponseDto, null));
    }

    @Operation(
            summary = "Update an existing set",
            description = "Update the information of a specific set owned by the authenticated user."
    )
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{setId}")
    public ResponseEntity<ApiResponse<SetResponseDto>> updateSet(
            @Parameter(description = "ID of the set to update", example = "5") @PathVariable Long setId,
            @Valid @RequestBody SetUpdatingRequestDto setUpdatingRequestDto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        SetResponseDto updatedSet = setService.updateSet(
                setId,
                (Long) jwt.getClaims().get("id"),
                setUpdatingRequestDto
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update set successfully", updatedSet, null));
    }

    @Operation(
            summary = "Delete a set",
            description = "Delete a specific set owned by the authenticated user."
    )
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{setId}")
    public ResponseEntity<ApiResponse<Object>> deleteSet(
            @Parameter(description = "ID of the set to delete", example = "5") @PathVariable(name = "setId") Long setId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        setService.deleteSet(setId, (Long) jwt.getClaims().get("id"));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete set successfully", null, null));
    }
}
