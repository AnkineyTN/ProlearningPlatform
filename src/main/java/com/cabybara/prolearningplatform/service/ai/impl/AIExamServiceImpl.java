package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.request.exam.EssayGradingRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.ExplainWrongAnswerRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByWebRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.EssayGradingResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExplainWrongAnswerResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIExamServiceImpl implements AIExamService {
    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String GENERATE_EXAM_BY_FILE_PATH = "/tests/from-file";
    private static final String GENERATE_EXAM_BY_NOTE_PATH = "/tests/from-note";
    private static final String GENERATE_EXAM_BY_WEB_PATH = "/tests/from-web";
    private static final String GRADE_ESSAY_PATH = "/tests/grade-essay";
    private static final String EXPLAIN_WRONG_ANSWER_PATH = "/tests/explain-answer";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    private String parseData(String json) {
        try {
            return objectMapper.readTree(json).path("data").asText("");
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse AI service response", e);
        }
    }

    private Resource convertToResource(MultipartFile file) throws IOException {
        return new ByteArrayResource(file.getBytes()) {
            @Override
            public String getFilename() {
                return file.getOriginalFilename();
            }
        };
    }

    @Override
    public GenerateExamByAIResponseDto generateExamByFiles(GenerateExamByFileRequestDto request) {
        try {
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            for (MultipartFile file : request.getFiles()) {
                body.add("files", convertToResource(file));
            }
            body.add("questions", request.getQuestions());
            body.add("difficulty", request.getDifficulty());
            body.add("free_text", request.getFreeText());
            body.add("language", request.getLanguage());

            String raw = restHttpClientUtil.postMultipart(
                    aiServiceBaseApi + GENERATE_EXAM_BY_FILE_PATH,
                    body,
                    String.class
            );

            return GenerateExamByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Exam by Files", e);
        }
    }

    @Override
    public GenerateExamByAIResponseDto generateExamByNotes(List<String> contents, Map<String, Integer> questions, Map<String, Double> difficulty, String freeText, Language language) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);
            body.put("questions", questions);
            body.put("difficulty", difficulty);
            body.put("free_text", freeText != null ? freeText : "");
            body.put("language", language != null ? language : "English");

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_EXAM_BY_NOTE_PATH,
                    body,
                    String.class
            );

            return GenerateExamByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Exam by Notes", e);
        }
    }

    @Override
    public GenerateExamByAIResponseDto generateExamByWeb(GenerateExamByWebRequestDto request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("urls", request.getUrls());
            body.put("questions", request.getQuestions());
            body.put("difficulty", request.getDifficulty());
            body.put("free_text", request.getFreeText() != null ? request.getFreeText() : "");
            body.put("language", request.getLanguage() != null ? request.getLanguage() : "English");

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_EXAM_BY_WEB_PATH,
                    body,
                    String.class
            );

            return GenerateExamByAIResponseDto.builder()
                    .content(parseData(raw))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Exam by Web", e);
        }
    }

    @Override
<<<<<<< HEAD
    public CreateExamFromReviewRequestDto generateExamFromReview(List<CardContent> cards) {
        // TODO: implement actual HTTP call to AI service

        return CreateExamFromReviewRequestDto.builder()
                .title("")
                .description("")
                .duration(0L)
                .questions(List.of())
                .build();
=======
    public GenerateExamByAIResponseDto generateExamFromReview(List<CardContent> cards) {
        // TODO: implement actual HTTP call to AI service when endpoint is ready

        return GenerateExamByAIResponseDto.builder().content("").build();
>>>>>>> dev
    }

    @Override
    public EssayGradingResponseDto gradeEssay(EssayGradingRequestDto request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("question_content", request.questionContent());
            body.put("expected_answer", request.expectedAnswer());
            body.put("student_answer", request.studentAnswer());
            body.put("language", "English");
            body.put("max_score", request.maxPoints());

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GRADE_ESSAY_PATH,
                    body,
                    String.class
            );

            // Parse the AI service response
            Map<String, Object> responseData = objectMapper.readValue(raw, Map.class);

            Double score = ((Number) responseData.get("score")).doubleValue();
            String feedback = (String) responseData.get("feedback");

            return new EssayGradingResponseDto(
                    request.attemptId(),
                    request.questionId(),
                    score,
                    request.maxPoints(),
                    feedback
            );

        } catch (Exception e) {
            log.error("Failed to call AI service for grading essay", e);
            throw new RuntimeException("Failed to call AI service for grading essay", e);
        }
    }

    @Override
    public ExplainWrongAnswerResponseDto explainWrongAnswer(ExplainWrongAnswerRequestDto request) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("question", request.question());
            body.put("correct_answer", request.correctAnswer());
            body.put("user_answer", request.userAnswer());
            body.put("language", request.language() != null ? request.language() : "English");

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + EXPLAIN_WRONG_ANSWER_PATH,
                    body,
                    String.class
            );

            // Parse the AI service response
            Map<String, Object> responseData = objectMapper.readValue(raw, Map.class);
            String explanation = (String) responseData.get("explanation");

            return new ExplainWrongAnswerResponseDto(explanation);

        } catch (Exception e) {
            log.error("Failed to call AI service for explaining wrong answer", e);
            throw new RuntimeException("Failed to call AI service for explaining wrong answer", e);
        }
    }
}