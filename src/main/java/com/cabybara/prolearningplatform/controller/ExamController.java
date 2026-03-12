package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.exam.*;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.ResponseData;
import com.cabybara.prolearningplatform.dto.response.ResponseError;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.service.exam.QuestionService;
import com.cabybara.prolearningplatform.service.exam.QuizService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    private final QuizService quizService;
    private final QuestionService questionService;

    @GetMapping()
    public ResponseEntity<ApiResponse<List<QuizResponseDto>>> getAllQuizzes(
            @PathVariable Long setId,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        List<QuizResponseDto> quizResponseDtos = quizService.getQuiz(setId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", quizResponseDtos, null));
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponseDto>> getQuiz(
            @PathVariable Long setId,
            @PathVariable Long quizId
    ) {
        QuizResponseDto quizResponseDto = quizService.getQuiz(setId, quizId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", quizResponseDto, null));
    }

    @PostMapping()
    public ResponseEntity<ApiResponse<QuizResponseDto>> createQuiz(
            @RequestBody @Valid CreateQuizRequestDto createQuizRequestDto,
            @PathVariable Long setId
    ) {
        QuizResponseDto quizResponseDto = quizService.createQuiz(setId, createQuizRequestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create exam successfully", quizResponseDto, null));
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<ApiResponse<QuizResponseDto>> updateQuiz(
            @PathVariable Long setId,
            @PathVariable Long quizId,
            @RequestBody @Valid UpdateQuizRequestDto updateQuizRequestDto
    ) {
        QuizResponseDto response = quizService.updateQuiz(setId, quizId, updateQuizRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update quiz successfully", response, null));
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<ApiResponse<String>> deleteQuiz(
            @PathVariable Long setId,
            @PathVariable Long quizId
    ) {
        quizService.deleteQuiz(setId, quizId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete quiz successfully", null, null));
    }

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponseDto>> getAllQuestionsByQuiz(
            @PathVariable Long setId,
            @PathVariable Long quizId,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        QuestionListResponseDto question = questionService.getQuestionsByQuizId(quizId);

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

    @GetMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponseDto>> getQuestionById(
            @PathVariable Long setId,
            @PathVariable Long quizId,
            @PathVariable Long questionId
    ) {
        QuestionResponseDto question = questionService.getQuestionById(quizId, questionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get question successfully", question, null));
    }

    @PostMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponseDto>> createQuestion(
            @PathVariable Long quizId,
            @RequestBody @Valid List<CreateQuestionRequestDto> createQuestionRequestDtos
    ) {
        QuestionListResponseDto response = questionService.createQuestion(quizId, createQuestionRequestDtos);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create question successfully", response, null));
    }

    @PutMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<QuestionResponseDto>> updateQuestion(
            @PathVariable Long setId,
            @PathVariable Long quizId,
            @PathVariable Long questionId,
            @RequestBody @Valid UpdateQuestionRequestDto request
    ) {
        QuestionResponseDto response = questionService.updateQuestion(quizId, questionId, request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update question successfully", response, null));
    }

    @DeleteMapping("/{quizId}/questions/{questionId}")
    public ResponseEntity<ApiResponse<Void>> deleteQuestion(
            @PathVariable Long setId,
            @PathVariable Long quizId,
            @PathVariable Long questionId
    ) {
        questionService.deleteQuestion(quizId, questionId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete question successfully", null, null));
    }

    // API AI
    @Operation(method = "POST", summary = "Generate exam by files with AI", description = "Generate exam by file with AI")
    @PostMapping(value = "/ai-file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseData<GenerateExamByAIResponseDto> generateExamByFile(
            @RequestPart("files") List<MultipartFile> files,
            @RequestPart("questions") String questions,
            @RequestPart("language") String language) {
        log.info("Generate exam by files with AI");
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Integer> questionsMap = mapper.readValue(questions, new TypeReference<>() {
            });

            GenerateExamByFileRequestDto request = GenerateExamByFileRequestDto.builder()
                    .files(files)
                    .questions(questionsMap)
                    .language(language)
                    .build();

            GenerateExamByAIResponseDto response = quizService.generateExamByFiles(request);
            return new ResponseData<>(HttpStatus.OK.value(), "Generate exam by files with AI successfully", response);
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e);
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "Generate exam by files with AI fail");
        }
    }
}
