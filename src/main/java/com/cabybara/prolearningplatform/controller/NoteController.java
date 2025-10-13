package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.*;
import com.cabybara.prolearningplatform.dto.response.*;
import com.cabybara.prolearningplatform.service.ai.AIService;
import com.cabybara.prolearningplatform.service.note.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/note")
@Validated
@Slf4j
@Tag(name = "Note APIs")
@RequiredArgsConstructor
public class NoteController {
    private final NoteService noteService;
    private final AIService aiService;
    private static final String ERROR_MESSAGE = "errorMessage={}";

    @Operation(method = "POST", summary = "Create note", description = "Create new note")
    @PostMapping(value = "/create")
    public ResponseData<Void> saveVideo(@Valid @RequestBody CreateNoteRequestDTO request) {
        log.info("Create note");
        try {
            noteService.createNote(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Create note successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Create note fail");
        }
    }

    @Operation(method = "PATCH", summary = "Save note", description = "Save note")
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

    @Operation(method = "POST", summary = "Convert file to vector DB", description = "Convert file to vector DB")
    @PostMapping(value = "/convert-to-vectordb")
    public ResponseData<Void> convertFileToVector(@Valid @RequestBody ConvertFileToVectorRequestDTO request) {
        log.info("Convert file to vector DB");
        try {
            aiService.convertFileToVector(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Convert file to vector successfully");
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Convert file to vector fail");
        }
    }

    @Operation(method = "POST", summary = "Explain text with AI", description = "Explain text with AI")
    @PostMapping(value = "/explain")
    public ResponseData<ExplainNoteResponseDTO> explainNote(@Valid @RequestBody ExplainNoteRequestDTO request) {
        log.info("Explain note with AI");
        try {
            ExplainNoteResponseDTO response = aiService.explainNote(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Explain note with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Explain note with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Summarize file with AI", description = "Summarize file with AI")
    @PostMapping(value = "/summarize")
    public ResponseData<SummarizeFileResponseDTO> explainNote(@Valid @RequestBody SummarizeFileRequestDTO request) {
        log.info("Summarize file with AI");
        try {
            SummarizeFileResponseDTO response = aiService.summarizeFile(request);
            return new ResponseData<>(HttpStatus.CREATED.value(), "Summarize file with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Summarize file with AI fail");
        }
    }

    @Operation(method = "GET", summary = "Get all notes of set", description = "Get all notes of set")
    @GetMapping(value = "/all/{setId}")
    public ResponseData<?> getAllNotesOfSet(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                            @Min(1) @RequestParam(defaultValue = "10", required = false) int pageSize,
                                            @PathVariable @Min(1) Long setId) {
        log.info("Get notes of set");
        return new ResponseData<>(HttpStatus.OK.value(), "Get videos by category", noteService.getAllNotesOfSet(pageNo, pageSize, setId));
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
}
