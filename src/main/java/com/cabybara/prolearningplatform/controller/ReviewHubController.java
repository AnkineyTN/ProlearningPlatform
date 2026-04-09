package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
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

    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final FlashcardMapper flashcardMapper;
    private final ExamMapper examMapper;
    private final AuthenticationContext authenticationContext;

    @GetMapping("/flashcards")
    public ResponseEntity<ApiResponse<List<FlashcardResponseDto>>> getReviewFlashcards(
            @ParameterObject @PageableDefault(page = 0, size = 10, sort = "createdAt") Pageable pageable
    ) {
        Long userId = authenticationContext.getCurrentUserId();
        Page<FlashcardResponseDto> page = flashcardRepository
                .findAllByUserIdAndCreateMethod(userId, CreationMethod.REVIEW_AI, pageable)
                .map(flashcardMapper::toFlashcardResponseDto);

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
        Long userId = authenticationContext.getCurrentUserId();
        Page<ExamResponseDto> page = examRepository
                .findAllByCreatedByAndCreationMethod(userId, CreationMethod.REVIEW_AI, pageable)
                .map(examMapper::toExamResponseDto);

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
