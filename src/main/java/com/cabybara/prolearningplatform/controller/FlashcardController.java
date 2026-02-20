package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/sets/{setId}/flashcards")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard")
public class FlashcardController {
    private final FlashcardService flashcardService;

    private static final String ERROR_MESSAGE = "errorMessage={}";

    @Operation(
            summary = "Get All Flashcards in a Set (Paginated)",
            description = "Retrieves a paginated list of all flashcards associated with a specific Set."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    public ResponseEntity<ApiResponse<List<FlashcardResponseDto>>> getAllFlashcard(
            @Parameter(description = "The ID of the Set to retrieve flashcards from", required = true)
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

    @Operation(
            summary = "Get a Specific Flashcard's Details",
            description = "Retrieves the full details of a single flashcard, including all its associated card items."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<DetailFlashcardResponseDto>> getDetailFlashcard(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to retrieve", required = true)
            @PathVariable Long flashcardId
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

    @Operation(
            summary = "Create a new Flashcard (Manual - Batch processing)",
            description = "Manually creates a new flashcard within a specific set. For cards with images, " +
                    "the 'imageAssetId' (obtained from the Image Upload endpoints) must be provided in the request body."
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/manual")
    public ResponseEntity<ApiResponse<FlashcardResponseDto>> createFlashcardManual(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,
            @Valid @RequestBody FlashcardCreateRequestDto flashcardCreateRequestDto
    ) {
        FlashcardResponseDto flashcardResponseDto = flashcardService.addFlashcardManual(setId, flashcardCreateRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create flashcard successfully", flashcardResponseDto, null));
    }

    @Operation(
            summary = "Update existing flashcard",
            description = "Update the flashcard title, description and privacy only"
    )
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<FlashcardResponseDto>> updateFlashcard(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to update", required = true)
            @PathVariable Long flashcardId,

            @Valid @RequestBody FlashcardUpdatingRequestDto flashcardUpdatingRequestDto
    ) throws BadRequestException {
        FlashcardResponseDto updatedFlashcard = flashcardService.updateFlashcard(
                setId,
                flashcardId,
                flashcardUpdatingRequestDto
        );

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update flashcard successfully", updatedFlashcard, null));
    }

    @Operation(
            summary = "Deletes a specific Flashcard from a Set",
            description = "Removes a Flashcard using its ID within the context of a specific Set ID"
    )
    @PreAuthorize("isAuthenticated()")
    @DeleteMapping("/{flashcardId}")
    public ResponseEntity<ApiResponse<String>> deleteFlashcard(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,

            @Parameter(description = "The ID of the Flashcard to delete", required = true)
            @PathVariable Long flashcardId
    ) throws BadRequestException {
        flashcardService.deleteFlashcard(setId, flashcardId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete flashcard successfully", null, null));
    }

    // API AI
    @Operation(method = "POST", summary = "Generate flashcard by files with AI", description = "Generate flashcard by file with AI")
    @PostMapping(value = "/ai-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<GenerateFlashcardByAIResponseDto> generateFlashcardByFile(@Valid @ModelAttribute GenerateFlashcardByFileRequestDto request) {
        log.info("Generate flashcard by files with AI");
        try {
            GenerateFlashcardByAIResponseDto response = flashcardService.generateFlashcardByFiles(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate flashcard by files with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate flashcard by files with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Generate flashcard by notes with AI", description = "Generate flashcard by note with AI")
    @PostMapping(value = "/ai-note")
    public ResponseData<GenerateFlashcardByAIResponseDto> generateFlashcardByNote(@Valid @RequestBody GenerateFlashcardByNoteRequestDto request) {
        log.info("Generate flashcard by notes with AI");
        try {
            GenerateFlashcardByAIResponseDto response = flashcardService.generateFlashcardByNotes(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate flashcard by notes with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate flashcard by notes with AI fail");
        }
    }

}
