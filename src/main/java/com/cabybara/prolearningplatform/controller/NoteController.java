package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.CreateNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.SaveNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.response.ResponseData;
import com.cabybara.prolearningplatform.dto.response.ResponseError;
import com.cabybara.prolearningplatform.service.NoteService;
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

    private static final String ERROR_MESSAGE = "errorMessage={}";

    // CREATE VIDEO
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
}
