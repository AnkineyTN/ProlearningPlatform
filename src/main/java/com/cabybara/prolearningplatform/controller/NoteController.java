package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.note.*;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.note.*;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.service.ai.AINoteService;
import com.cabybara.prolearningplatform.service.note.NoteService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequestMapping("/note")
@Validated
@Slf4j
@Tag(name = "Note APIs")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final AINoteService aiNoteService;
    private static final String ERROR_MESSAGE = "errorMessage={}";

    @Operation(method = "POST", summary = "Create note", description = "Create new note")
    @PostMapping(value = "/create")
    public ResponseData<CreateNoteResponseDTO> createNote(@Valid @RequestBody CreateNoteRequestDTO request) {
        log.info("Create note");
        try {
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create note successfully", noteService.createNote(request));
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Create note fail");
        }
    }

    @Operation(method = "PATCH", summary = "Save note", description = "Save note while taking note")
    @PatchMapping(value = "/save/{noteId}")
    public ResponseData<Void> saveNote(@PathVariable @Min(1) Long noteId, @Valid @RequestBody SaveNoteRequestDTO request) {
        log.info("Save note, noteId={}", noteId);
        try {
            noteService.saveNote(noteId, request);
            return new ResponseData<>(HttpStatus.OK.value(), "Save note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Save note fail");
        }
    }

    @Operation(method = "POST", summary = "Save document in note", description = "Save document to note_docs table after uploading to cloudinary and having assetId")
    @PostMapping(value = "/save-doc")
    public ResponseData<Void> saveDocumentInNote(@Valid @RequestBody SaveDocInNoteRequestDto request) {
        log.info("Save document in note");
        try {
            noteService.saveDocInNote(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Save document in note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Save document in note fail");
        }
    }

    @Operation(method = "POST", summary = "Save image in note", description = "Save image to note_imgs table after uploading to cloudinary and having assetId")
    @PostMapping(value = "/save-img")
    public ResponseData<Void> saveImageInNote(@Valid @RequestBody SaveImgInNoteRequestDto request) {
        log.info("Save image in note");
        try {
            noteService.saveImgInNote(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Save image in note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Save image in note fail");
        }
    }

    @Operation(method = "GET", summary = "Get all notes of set", description = "Get all notes of set")
    @GetMapping(value = "/all/{setId}")
    @ValidateSort(allowedFields = {"id", "created_at", "updated_at", "title"})
//    public ResponseData<?> getAllNotesOfSet(@RequestParam(defaultValue = "0", required = false) int pageNo,
//                                            @Min(1) @RequestParam(defaultValue = "10", required = false) int pageSize,
//                                            @PathVariable @Min(1) Long setId) {
    public ResponseEntity<ApiResponse<List<GetAllNotesResponseDTO>>> getAllNotesOfSet(
            @PathVariable Long setId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Privacy privacy,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {

        Page<GetAllNotesResponseDTO> noteResponseDtos = noteService.getAllNotes(setId, q, privacy, pageable);
        PaginationResponseDto paginationResponseDto = PaginationResponseDto.builder()
                .currentPage(noteResponseDtos.getNumber())
                .totalPages(noteResponseDtos.getTotalPages())
                .totalItems(noteResponseDtos.getTotalElements())
                .pageSize(noteResponseDtos.getSize())
                .build();


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(
                        "Successfully",
                        noteResponseDtos.getContent(),
                        paginationResponseDto
                ));

//        log.info("Get notes of set");
//        return new ResponseData<>(HttpStatus.OK.value(), "Get videos by category", noteService.getAllNotesOfSet(pageNo, pageSize, setId));
    }

    @Operation(summary = "Get note detail", description = "Get note detail")
    @GetMapping("/{noteId}")
    public ResponseData<GetDetailNoteResponseDTO> getDetailNote(@PathVariable @Min(1) Long noteId) {
        try {
            log.info("Get note detail, noteId={}", noteId);
            return new ResponseData<>(HttpStatus.OK.value(), "Note detail", noteService.getDetailNote(noteId));
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), e.getMessage());
        }
    }

    @Operation(summary = "Delete document in note", description = "Delete document in note")
    @DeleteMapping("/delete-doc")
    public ResponseData<Void> deleteDocInNote(@Valid @RequestBody DeleteNoteDocRequestDTO request) {
        log.info("Delete document in note, notedId={}, assetId={}", request.getNoteId(), request.getAssetId() );
        try {
            noteService.deleteDocInNote(request);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Delete document in note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Delete document in note fail");
        }
    }

    @Operation(summary = "Delete image in note", description = "Delete image in note")
    @DeleteMapping("/delete-img")
    public ResponseData<Void> deleteImgInNote(@Valid @RequestBody DeleteNoteImgRequestDTO request) {
        log.info("Delete image in note, fileUrl={}", request.getFileUrl());
        try {
            noteService.deleteImgInNote(request);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Delete image in note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Delete image in note fail");
        }
    }

    @Operation(method = "PATCH", summary = "Update note", description = "Update note")
    @PatchMapping(value = "/update/{noteId}")
    public ResponseData<Void> updateNote(@PathVariable @Min(1) Long noteId, @Valid @RequestBody UpdateNoteRequestDTO request) {
        log.info("Update note, noteId={}", noteId);
        try {
            noteService.updateNote(noteId, request);
            return new ResponseData<>(HttpStatus.OK.value(), "Update note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Update note fail");
        }
    }

    @Operation(summary = "Delete note", description = "Delete note permanently")
    @DeleteMapping("/delete/{noteId}")
    public ResponseData<Void> deleteNote(@Min(value = 1) @PathVariable Long noteId) {
        log.info("Delete note, noteId={}", noteId);
        try {
            noteService.deleteNote(noteId);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "Delete note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Delete note fail");
        }
    }

    // =============================================
    // ==== AI API
    // =============================================
    @Operation(method = "POST", summary = "Convert file to vector DB", description = "Convert file to vector DB to query when explaining with AI")
    @PostMapping(value = "/convert-to-vectordb")
    public ResponseData<Void> convertFileToVector(@Valid @RequestBody ConvertFileToVectorRequestDTO request) {
        log.info("Convert file to vector DB");
        try {
            aiNoteService.convertFileToVector(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Convert file to vector successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Convert file to vector fail");
        }
    }

    @Operation(method = "POST", summary = "Explain note with AI", description = "Explain selected text in note with AI")
    @PostMapping(value = "/explain")
    public ResponseData<ExplainNoteResponseDTO> explainNote(@Valid @RequestBody ExplainNoteRequestDTO request) {
        log.info("Explain note with AI");
        try {
            ExplainNoteResponseDTO response = aiNoteService.explainNote(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Explain note with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Explain note with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Summarize file with AI", description = "Summarize file with AI")
    @PostMapping(value = "/summarize")
    public ResponseData<SummarizeFileResponseDTO> summarizeFile(@Valid @RequestBody SummarizeFileRequestDTO request) {
        log.info("Summarize file with AI");
        try {
            SummarizeFileResponseDTO response = aiNoteService.summarizeFile(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Summarize file with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Summarize file with AI fail");
        }
    }
}
