package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.service.favorite.UserFavoriteService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/me/favorites")
@RequiredArgsConstructor
@Tag(name = "User Favorites")
@Validated
@PreAuthorize("isAuthenticated()")
public class UserFavoriteController {

    private final UserFavoriteService userFavoriteService;
    private final AuthenticationContext authenticationContext;

    @PostMapping("/{type}/{id}")
    @Operation(summary = "Toggle favorite status for a resource")
    public ResponseEntity<ApiResponse<Boolean>> toggleFavorite(
            @PathVariable ContentType type,
            @PathVariable Long id
    ) {
        Long userId = authenticationContext.getCurrentUserId();
        boolean isFavorited = userFavoriteService.toggleFavorite(userId, id, type);
        String message = isFavorited ? "Added to favorites" : "Removed from favorites";
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success(message, isFavorited, null));
    }

    @GetMapping
    @Operation(summary = "Get list of favorite resources")
    public ResponseEntity<ApiResponse<?>> getFavorites(
            @RequestParam(required = false) ContentType type,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = authenticationContext.getCurrentUserId();
        Page<SocialItemResponseDto> page = userFavoriteService.getFavoriteResources(userId, type, pageable);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully retrieved favorites", page.getContent(), buildPagination(page)));
    }

    @GetMapping("/{type}")
    @Operation(summary = "Get list of favorite resources by specific type")
    public ResponseEntity<ApiResponse<?>> getFavoritesByType(
            @PathVariable ContentType type,
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt", direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable
    ) {
        Long userId = authenticationContext.getCurrentUserId();
        Page<SocialItemResponseDto> page = userFavoriteService.getFavoriteResources(userId, type, pageable);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully retrieved favorites", page.getContent(), buildPagination(page)));
    }

    private PaginationResponseDto buildPagination(Page<?> page) {
        return PaginationResponseDto.builder()
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .pageSize(page.getSize())
                .build();
    }
}
