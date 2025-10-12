package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.SetUpdateRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.cabybara.prolearningplatform.dto.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.SetResponseDto;
import com.cabybara.prolearningplatform.service.SetService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sets")
@RequiredArgsConstructor
public class SetController {
    private final SetService setService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public ResponseEntity<ApiResponse<Object>> getSet(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
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

    @PreAuthorize("isAuthenticated()")
    @PostMapping("")
    public ResponseEntity<ApiResponse<SetResponseDto>> createSet(
        @Valid @RequestBody SetCreationRequestDto setCreationRequestDto,
        @AuthenticationPrincipal Jwt jwt
    ) {

        SetResponseDto setResponseDto = setService.createSet( (Long) jwt.getClaims().get("id"), setCreationRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Create set successfully", setResponseDto, null));
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{setId}")
    public ResponseEntity<ApiResponse<SetResponseDto>> updateSet(
            @PathVariable Long setId,
            @RequestBody SetUpdateRequestDto setUpdateRequestDto,
            @AuthenticationPrincipal Jwt jwt
    ) {

        SetResponseDto updatedSet = setService.updateSet(
                setId,
                (Long) jwt.getClaims().get("id"),
                setUpdateRequestDto
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update set successfully", updatedSet, null));
    }

    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{setId}")
    public ResponseEntity<ApiResponse<Object>> deleteSet(
            @PathVariable(name = "setId") Long setId,
            @AuthenticationPrincipal Jwt jwt
    ) {

        setService.deleteSet(setId, (Long) jwt.getClaims().get("id"));

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete set successfully", null, null));
    }
}
