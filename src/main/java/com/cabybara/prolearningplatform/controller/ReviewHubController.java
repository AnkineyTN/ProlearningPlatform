package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.service.review.ReviewHubService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/review-hub")
@RequiredArgsConstructor
@Tag(name = "Review Hub")
@PreAuthorize("isAuthenticated()")
public class ReviewHubController {

    private final ReviewHubService reviewHubService;

    @GetMapping("/flashcards")
    public ResponseEntity<ApiResponse<List<FlashcardResponseDto>>> getReviewFlashcards(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<FlashcardResponseDto> page = reviewHubService.getReviewFlashcards(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(),
                        PaginationResponseDto.builder()
                                .currentPage(page.getNumber())
                                .totalPages(page.getTotalPages())
                                .totalItems(page.getTotalElements())
                                .pageSize(page.getSize())
                                .build()));
    }

    @GetMapping("/exams")
    public ResponseEntity<ApiResponse<List<ExamResponseDto>>> getReviewExams(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable
    ) {
        Page<ExamResponseDto> page = reviewHubService.getReviewExams(pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", page.getContent(),
                        PaginationResponseDto.builder()
                                .currentPage(page.getNumber())
                                .totalPages(page.getTotalPages())
                                .totalItems(page.getTotalElements())
                                .pageSize(page.getSize())
                                .build()));
    }
}
