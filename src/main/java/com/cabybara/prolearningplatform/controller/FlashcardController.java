package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.flashcard.*;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.request.share.UpdateMemberRoleRequest;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.NoteMemberResponse;
import com.cabybara.prolearningplatform.dto.response.user.UserSearchResponse;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.permission.impl.FlashcardPermissionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import com.cabybara.prolearningplatform.service.permission.annotation.AiRateLimit;

@Slf4j
@RestController
@RequestMapping("/sets/{setId}/flashcards")
@RequiredArgsConstructor
@Validated
@Tag(name = "Flashcard")
public class FlashcardController {
    private static final String ERROR_MESSAGE = "errorMessage={}";

    private final FlashcardService flashcardService;
    private final AIFlashcardService aIFlashcardService;
    private final FlashcardPermissionService flashcardPermissionService;
    private final AuthenticationContext authenticationContext;

    @Operation(
            summary = "Get All Flashcards in a Set (Paginated)",
            description = "Retrieves a paginated list of all flashcards associated with a specific Set."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("")
    @ValidateSort(allowedFields = {"id", "created_at", "updated_at", "title"})
    public ResponseEntity<ApiResponse<List<FlashcardResponseDto>>> getAllFlashcard(
            @Parameter(description = "The ID of the Set to retrieve flashcards from", required = true)
            @PathVariable Long setId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Privacy privacy,
            @RequestParam(required = false) CreationMethod createMethod,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        Page<FlashcardResponseDto> allFlashcardResponseDtos = flashcardService.getAllFlashcard(setId, q, privacy, createMethod, pageable);
        PaginationResponseDto paginationResponseDto = PaginationResponseDto.builder()
                .currentPage(allFlashcardResponseDtos.getNumber())
                .totalPages(allFlashcardResponseDtos.getTotalPages())
                .totalItems(allFlashcardResponseDtos.getTotalElements())
                .pageSize(allFlashcardResponseDtos.getSize())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(
                        "Successfully",
                        allFlashcardResponseDtos.getContent(),
                        paginationResponseDto
                ));
    }

    @Operation(
            summary = "Get a Specific Flashcard's Details",
            description = "Retrieves the full details of a single flashcard, including all its associated card items."
    )
    @PreAuthorize("isAuthenticated() and @flashcardPermissionService.hasAccess(@authenticationContext.getCurrentUserId(), #flashcardId)")
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

    // =============================================
    // ==== AI API
    // =============================================
    @Operation(method = "POST", summary = "Generate flashcard by files with AI", description = "Generate flashcard by file with AI")
    @PostMapping(value = "/ai-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @AiRateLimit(type = "AI_GENERATION")
    public ResponseData<GenerateFlashcardByAIResponseDto> generateFlashcardByFile(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,
            @Valid @ModelAttribute GenerateFlashcardByFileRequestDto request
    ) {
        log.info("Generate flashcard by files with AI");
        try {
            GenerateFlashcardByAIResponseDto response = aIFlashcardService.generateFlashcardByFiles(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate flashcard by files with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate flashcard by files with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Generate flashcard by notes with AI", description = "Generate flashcard by note with AI")
    @PostMapping(value = "/ai-note")
    @AiRateLimit(type = "AI_GENERATION")
    public ResponseData<GenerateFlashcardByAIResponseDto> generateFlashcardByNote(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,
            @Valid @RequestBody GenerateFlashcardByNoteRequestDto request
    ) {
        log.info("Generate flashcard by notes with AI");
        try {
            GenerateFlashcardByAIResponseDto response = flashcardService.generateFlashcardByNotes(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate flashcard by notes with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate flashcard by notes with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Generate flashcard by web URL with AI", description = "Generate flashcard by web URL with AI")
    @PostMapping(value = "/ai-web")
    @AiRateLimit(type = "AI_GENERATION")
    public ResponseData<GenerateFlashcardByAIResponseDto> generateFlashcardByWeb(
            @Parameter(description = "The ID of the Set", required = true)
            @PathVariable Long setId,
            @Valid @RequestBody GenerateFlashcardByWebRequestDto request
    ) {
        log.info("Generate flashcard by notes with AI");
        try {
            GenerateFlashcardByAIResponseDto response = aIFlashcardService.generateFlashcardByWeb(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate flashcard by web with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate flashcard by web with AI fail");
        }
    }

    @PostMapping("/{flashcardId}/members/invite")
    @PreAuthorize("@flashcardPermissionService.isOwner(@authenticationContext.getCurrentUserId(), #flashcardId)")
    public ResponseEntity<ApiResponse<List<InviteResultResponse>>> inviteMembers(
        @PathVariable Long setId,
        @PathVariable Long flashcardId,
        @RequestBody InviteMemberRequest request
    ) {
        List<InviteResultResponse> results = flashcardService.inviteMembers(setId, flashcardId, request);

        boolean hasFailure = results.stream().anyMatch(r -> !r.isSuccess());

        return ResponseEntity.status(hasFailure ? HttpStatus.MULTI_STATUS : HttpStatus.OK).body(
            ResponseUtil.success(
                "Invitation process completed",
                results,
                null
            )
        );
    }
    
    @PostMapping("/{flashcardId}/members/accept")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> acceptInvite(
        @PathVariable Long setId,
        @PathVariable Long flashcardId
    ) {
        flashcardService.acceptInvite(flashcardId);
        return ResponseEntity.ok(
            ResponseUtil.success("Invite accepted successfully", null, null)
        );
    }

    @PostMapping("/{flashcardId}/members/decline")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> declineInvite(
        @PathVariable Long setId,
        @PathVariable Long flashcardId
    ) {
        flashcardService.declineInvite(flashcardId);
        return ResponseEntity.ok(
            ResponseUtil.success("Invite declined successfully", null, null)
        );
    }

    @DeleteMapping("/{flashcardId}/members/{targetUserId}")
    @PreAuthorize("@flashcardPermissionService.isOwner(@authenticationContext.getCurrentUserId(), #flashcardId)")
    public ResponseEntity<ApiResponse<Void>> removeMember(
        @PathVariable Long setId,
        @PathVariable Long flashcardId,
        @PathVariable Long targetUserId
    ) {
        flashcardService.removeMember(flashcardId, targetUserId);
        return ResponseEntity.ok(
            ResponseUtil.success("Member removed successfully", null, null)
        );
    }

    @GetMapping("/{flashcardId}/members")
    @PreAuthorize("isAuthenticated() and @flashcardPermissionService.hasAccess(@authenticationContext.getCurrentUserId(), #flashcardId)")
    public ResponseEntity<ApiResponse<List<NoteMemberResponse>>> getMembers(
        @PathVariable Long setId,
        @PathVariable Long flashcardId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        Page<NoteMemberResponse> result = (keyword != null && !keyword.isBlank())
        ? flashcardPermissionService.searchMembers(flashcardId, keyword, pageable)
        : flashcardPermissionService.getMembers(flashcardId, pageable);

        PaginationResponseDto pagination = PaginationResponseDto.builder()
            .currentPage(result.getNumber())
            .totalPages(result.getTotalPages())
            .totalItems(result.getTotalElements())
            .pageSize(result.getSize())
            .build();

        return ResponseEntity.ok(
            ResponseUtil.success(
                "Members retrieved successfully",
                result.getContent(),
                pagination
            )
        );
    }

    @PatchMapping("/{flashcardId}/members/{targetUserId}/role")
    @PreAuthorize("@flashcardPermissionService.isOwner(@authenticationContext.getCurrentUserId(), #flashcardId)")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable Long setId,
            @PathVariable Long flashcardId,
            @PathVariable Long targetUserId,
            @RequestBody UpdateMemberRoleRequest request
    ) {
        flashcardPermissionService.updateMemberRole(
            flashcardId, targetUserId, request.getRole(),
            authenticationContext.getCurrentUserId()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
            ResponseUtil.success("Member role updated successfully", null, request)
        );
    }

    @GetMapping("/{flashcardId}/users/search")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<UserSearchResponse>>> searchUsers(
        @PathVariable Long setId,
        @PathVariable Long flashcardId,
        @RequestParam(required = false) String keyword,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<UserSearchResponse> result = flashcardPermissionService.searchUsers(keyword, flashcardId, pageable);

        PaginationResponseDto pagination = PaginationResponseDto.builder()
            .currentPage(result.getNumber())
            .totalPages(result.getTotalPages())
            .totalItems(result.getTotalElements())
            .pageSize(result.getSize())
            .build();

        return ResponseEntity.ok(
            ResponseUtil.success(
                "Users retrieved successfully",
                result.getContent(),
                pagination
            )
        );
    }
}
