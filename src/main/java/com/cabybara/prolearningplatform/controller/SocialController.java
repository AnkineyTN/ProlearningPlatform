package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.service.social.SocialService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
@Tag(name = "Social")
@Validated
public class SocialController {

    private final SocialService socialService;

    @GetMapping("/notes")
    @Operation(summary = "Browse public notes")
    @ValidateSort(allowedFields = {"title", "id", "created_at", "updated_at"})
    public ResponseEntity<ApiResponse<?>> getPublicNotes(
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(page = 0, size = 12, sort = "title") Pageable pageable
    ) {
        Page<SocialItemResponseDto> page = socialService.getSocialNotes(q, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), buildPagination(page)));
    }

    @GetMapping("/flashcards")
    @Operation(summary = "Browse public flashcards")
    @ValidateSort(allowedFields = {"title", "id", "created_at", "updated_at"})
    public ResponseEntity<ApiResponse<?>> getPublicFlashcards(
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(page = 0, size = 12, sort = "title") Pageable pageable
    ) {
        Page<SocialItemResponseDto> page = socialService.getSocialFlashcards(q, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), buildPagination(page)));
    }

    @GetMapping("/exams")
    @Operation(summary = "Browse public exams")
    @ValidateSort(allowedFields = {"title", "id", "created_at", "updated_at"})
    public ResponseEntity<ApiResponse<?>> getPublicExams(
            @RequestParam(required = false) String q,
            @ParameterObject @PageableDefault(page = 0, size = 12, sort = "title") Pageable pageable
    ) {
        Page<SocialItemResponseDto> page = socialService.getSocialExams(q, pageable);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(), buildPagination(page)));
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
