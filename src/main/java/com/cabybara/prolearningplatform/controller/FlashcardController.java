package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/sets/{setId}/flashcards")
@RequiredArgsConstructor
@Validated
public class FlashcardController {
    private final FlashcardService flashcardService;

    @GetMapping("")
    public ResponseEntity<ApiResponse<?>> getAllFlashcard(
            @PathVariable Long setId,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        Page<FlashcardResponseDto> allFlashcardResponseDtos = flashcardService.getAllFlashcard(setId, pageable);
        PaginationResponseDto paginationResponseDto = PaginationResponseDto.builder()
                .currentPage(allFlashcardResponseDtos.getNumber())
                .totalPages(allFlashcardResponseDtos.getTotalPages())
                .totalItems(allFlashcardResponseDtos.getTotalElements())
                .pageSize(allFlashcardResponseDtos.getSize())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(
                        "Successfully get all flashcard",
                        allFlashcardResponseDtos.getContent(),
                        paginationResponseDto
                ));
    }

    @GetMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<?>> getDetailFlashcard(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        DetailFlashcardResponseDto detailFlashcardResponseDto = flashcardService.getDetailFlashcard(
                setId, flashcardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(
                        "Successfully get all flashcard",
                        detailFlashcardResponseDto,
                        null
                ));
    }

    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<String>> createFlashcardManual(
            @PathVariable Long setId,
            @RequestBody FlashcardCreateRequestDto flashcardCreateRequestDto
    ) {
        flashcardService.addFlashcardManual(setId, flashcardCreateRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create flashcard successfully", null, null));
    }

    @PostMapping("/import")
    public ResponseEntity<ApiResponse<String>> createFlashcardFromImport(
            @PathVariable Long setId,
            @RequestBody FlashcardCreateRequestDto flashcardCreateRequestDto
    ) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create flashcard successfully", null, null));
    }
}
