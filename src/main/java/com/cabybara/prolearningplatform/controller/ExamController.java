package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.exam.*;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.ResponseData;
import com.cabybara.prolearningplatform.dto.response.ResponseError;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamAttemptResultDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamQuestionViewDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.ExamAttemptService;
import com.cabybara.prolearningplatform.service.exam.QuestionService;
import com.cabybara.prolearningplatform.service.exam.ExamService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.cabybara.prolearningplatform.utils.ValidateSort;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static jakarta.servlet.RequestDispatcher.ERROR_MESSAGE;

@RestController
@RequestMapping("/set/{setId}/exams")
@RequiredArgsConstructor
@Tag(name = "Exams")
@Validated
@Slf4j
public class ExamController {
    private final ExamService examService;
    private final QuestionService questionService;
    private final AIExamService aiExamService;
    private final ExamAttemptService examAttemptService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping()
    @ValidateSort(allowedFields = {"id", "created_at", "updated_at", "title"})
    public ResponseEntity<ApiResponse<List<ExamResponseDto>>> getAllExams(
            @PathVariable Long setId,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Privacy privacy,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        Page<ExamResponseDto> examResponseDtos = examService.getExam(setId, q, privacy, pageable);
        PaginationResponseDto paginationResponseDto = PaginationResponseDto.builder()
                .currentPage(examResponseDtos.getNumber())
                .totalPages(examResponseDtos.getTotalPages())
                .totalItems(examResponseDtos.getTotalElements())
                .pageSize(examResponseDtos.getSize())
                .build();


        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(
                        "Successfully",
                        examResponseDtos.getContent(),
                        paginationResponseDto
                ));
    }

    @GetMapping("/{examId}")
    public ResponseEntity<ApiResponse<ExamResponseDto>> getExam(
            @PathVariable Long setId,
            @PathVariable Long examId
    ) {
        ExamResponseDto examResponseDto = examService.getExam(setId, examId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", examResponseDto, null));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<ExamResponseDto>> createExam(
            @RequestBody @Valid CreateExamRequestDto createExamRequestDto,
            @PathVariable Long setId
    ) {
        ExamResponseDto examResponseDto = examService.createExam(setId, createExamRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create exam successfully", examResponseDto, null));
    }

    @PutMapping("/{examId}")
    public ResponseEntity<ApiResponse<ExamResponseDto>> updateExam(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @RequestBody @Valid UpdateExamRequestDto updateExamRequestDto
    ) {
        ExamResponseDto response = examService.updateExam(setId, examId, updateExamRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update exam successfully", response, null));
    }

    @DeleteMapping("/{examId}")
    public ResponseEntity<ApiResponse<String>> deleteExam(
            @PathVariable Long setId,
            @PathVariable Long examId
    ) {
        examService.deleteExam(setId, examId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete exam successfully", null, null));
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{examId}/questions/take")
    public ResponseEntity<ApiResponse<List<ExamQuestionViewDto>>> getQuestionsForTaking(
            @PathVariable Long setId,
            @PathVariable Long examId
    ) {
        List<ExamQuestionViewDto> questions = questionService.getQuestionsForTaking(examId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get questions successfully", questions, null));
    }

    @GetMapping("/{examId}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponseDto>> getAllQuestionsByExam(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        QuestionListResponseDto question = questionService.getQuestionsByExamId(examId);

        List<QuestionResponseDto> allQuestions = question.questions();

        int totalItems = allQuestions.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, totalItems);

        List<QuestionResponseDto> pagedQuestions =
                start >= totalItems ? List.of() : allQuestions.subList(start, end);

        QuestionListResponseDto pagedResponse = new QuestionListResponseDto(pagedQuestions);

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get questions successfully", pagedResponse, PaginationResponseDto.builder()
                        .pageSize(pageSize)
                        .currentPage(currentPage)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .build()
                ));
    }

    @GetMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponseDto>> getQuestionById(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @PathVariable Long questionId
    ) {
        QuestionResponseDto question = questionService.getQuestionById(examId, questionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get question successfully", question, null));
    }

    @PostMapping("/{examId}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponseDto>> createQuestion(
            @PathVariable Long examId,
            @RequestBody @Valid List<CreateQuestionRequestDto> createQuestionRequestDtos
    ) {
        QuestionListResponseDto response = questionService.createQuestion(examId, createQuestionRequestDtos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create question successfully", response, null));
    }

    @PutMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponseDto>> updateQuestion(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @PathVariable Long questionId,
            @RequestBody @Valid UpdateQuestionRequestDto request
    ) {
        QuestionResponseDto response = questionService.updateQuestion(examId, questionId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update question successfully", response, null));
    }

    @DeleteMapping("/{examId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @PathVariable Long questionId
    ) {
        questionService.deleteQuestion(examId, questionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete question successfully", null, null));
    }

    @Operation(
            summary = "Start a new exam attempt",
            description = "Initializes a new attempt record for the specified exam and returns the initial attempt data."
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{examId}/attempts")
    public ResponseEntity<ApiResponse<ExamAttemptDto>> startAttempt(
            @PathVariable Long setId,
            @PathVariable Long examId
    ) {
        ExamAttemptDto attempt = examAttemptService.startAttempt(examId);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Attempt started", attempt, null));
    }

    @Operation(
            summary = "Submit an exam attempt",
            description = "Submits the user's answers, calculates the score, and marks the attempt as completed."
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{examId}/attempts/{attemptId}/submit")
    public ResponseEntity<ApiResponse<ExamAttemptResultDto>> submitAttempt(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @PathVariable Long attemptId,
            @RequestBody @Valid SubmitExamRequestDto request
    ) {
        ExamAttemptResultDto result = examAttemptService.submitAttempt(examId, attemptId, request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Attempt submitted", result, null));
    }

    @Operation(
            summary = "Get attempt history",
            description = "Retrieves a list of all previous attempts made by the current user for this specific exam."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{examId}/attempts")
    public ResponseEntity<ApiResponse<List<ExamAttemptDto>>> getAttemptHistory(
            @PathVariable Long setId,
            @PathVariable Long examId
    ) {
        List<ExamAttemptDto> history = examAttemptService.getAttemptHistory(examId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", history, null));
    }

    @Operation(
            summary = "Get attempt details",
            description = "Fetches detailed results, including scoring and feedback, for a specific attempt ID."
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{examId}/attempts/{attemptId}")
    public ResponseEntity<ApiResponse<ExamAttemptResultDto>> getAttemptDetail(
            @PathVariable Long setId,
            @PathVariable Long examId,
            @PathVariable Long attemptId
    ) {
        ExamAttemptResultDto result = examAttemptService.getAttemptDetail(examId, attemptId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", result, null));
    }

    // =============================================
    // ==== AI API
    // =============================================
    @Operation(method = "POST", summary = "Generate exam by files with AI", description = "Generate exam by file with AI")
    @PostMapping(value = "/ai-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<GenerateExamByAIResponseDto> generateExamByFile(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("questions") String questions,
            @RequestPart("difficulty") String difficulty,
            @RequestPart(value = "freeText", required = false) String freeText,
            @RequestPart("language") String language) {
        log.info("Generate exam by files with AI");
        try {
            GenerateExamByFileRequestDto request = GenerateExamByFileRequestDto.builder()
                    .files(files)
                    .questions(questions)
                    .difficulty(difficulty)
                    .freeText(freeText)
                    .language(language)
                    .build();

            GenerateExamByAIResponseDto response = aiExamService.generateExamByFiles(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate exam by files with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate exam by files with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Generate exam by note with AI", description = "Generate exam by note with AI")
    @PostMapping(value = "/ai-note")
    public ResponseData<GenerateExamByAIResponseDto> generateExamByNote(@RequestBody GenerateExamByNoteRequestDto request) {
        log.info("Generate exam by note with AI");
        try {
            GenerateExamByAIResponseDto response = examService.generateExamByNotes(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate exam by note with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate exam by note with AI fail");
        }
    }

    @Operation(method = "POST", summary = "Generate flashcard by web URL with AI", description = "Generate exam by web URL with AI")
    @PostMapping(value = "/ai-web")
    public ResponseData<GenerateExamByAIResponseDto> generateExamByWeb(@Valid @RequestBody GenerateExamByWebRequestDto request) {
        log.info("Generate exam by notes with AI");
        try {
            GenerateExamByAIResponseDto response = aiExamService.generateExamByWeb(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate exam by web with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate exam by web with AI fail");
        }
    }
}
