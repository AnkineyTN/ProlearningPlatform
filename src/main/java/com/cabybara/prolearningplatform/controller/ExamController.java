package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.UpdateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import com.cabybara.prolearningplatform.service.exam.QuestionService;
import com.cabybara.prolearningplatform.service.exam.QuizService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        List<QuizResponseDto> quizResponseDtos = quizService.getQuizzes(setId, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", quizResponseDtos, null));
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

    @GetMapping("/{quizId}/questions")
    public ResponseEntity<ApiResponse<QuestionListResponseDto>> getQuestionsByQuiz(
            @PathVariable Long setId,
            @PathVariable Long quizId
    ) {
        QuestionListResponseDto questions = questionService.getQuestionsByQuizId(quizId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get questions successfully", questions, null));
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
}
