package com.cabybara.prolearningplatform.service.ai.impl;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.internal.QuestionContent;
import com.cabybara.prolearningplatform.dto.request.exam.*;
import com.cabybara.prolearningplatform.dto.response.exam.EssayGradingResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExplainWrongAnswerResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.JsonNode;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AIExamServiceImpl implements AIExamService {
    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################

    @Value("${aiservice.api}")
    private String aiServiceBaseApi;

    private static final String GENERATE_EXAM_BY_FILE_PATH = "/tests/from-file";
    private static final String GENERATE_EXAM_BY_NOTE_PATH = "/tests/from-note";
    private static final String GENERATE_EXAM_BY_WEB_PATH = "/tests/from-web";
    private static final String GRADE_ESSAY_PATH = "/tests/grade-essay";
    private static final String EXPLAIN_WRONG_ANSWER_PATH = "/tests/explain-answer";
    private static final String GENERATE_EXAM_FROM_FORGOTTEN_FLASHCARD = "/tests/from-forgotten-cards";
    private static final String GENERATE_EXAM_FROM_WRONG_ANSWERS = "/tests/from-wrong-answers";

    private final RestHttpClientUtil restHttpClientUtil;
    private final ObjectMapper objectMapper;

    // ##################################################
    // #################  UTILS METHOD  #################
    // ##################################################

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

    // MCQ|Question content|opt1|opt2|opt3|opt4|correctIndex (1-based)
    private CreateQuestionRequestDto parseMCQ(String[] parts) {
        // parts[0] = "MCQ"
        // parts[1] = question content
        // parts[2..n-1] = options
        // parts[n] = correct answer index (1-based)

        String content = parts[1].trim();

        int correctIndex = Integer.parseInt(parts[parts.length - 1].trim()); // 1-based

        List<QuestionOptionDto> options = new ArrayList<>();
        for (int i = 2; i < parts.length - 1; i++) {
            boolean isCorrect = (i - 1) == correctIndex; // i-1 converts to 1-based
            options.add(new QuestionOptionDto(null, parts[i].trim(), isCorrect));
        }

        return new CreateQuestionRequestDto(content, "MULTI_CHOICE", 1, options, null);
    }

    // TF|Question content|True or False
    private CreateQuestionRequestDto parseTF(String[] parts) {
        String content = parts[1].trim();
        String answer = parts[2].trim(); // "True" or "False"

        List<QuestionOptionDto> options = List.of(
                new QuestionOptionDto(null, "True", answer.equalsIgnoreCase("True")),
                new QuestionOptionDto(null, "False", answer.equalsIgnoreCase("False"))
        );

        return new CreateQuestionRequestDto(content, "TRUE_FALSE", 1, options, null);
    }

    // ESS|Question content|Expected answer
    private CreateQuestionRequestDto parseESS(String[] parts) {
        String content = parts[1].trim();
        String expectedAnswer = parts.length > 2 ? parts[2].trim() : null;

        return new CreateQuestionRequestDto(content, "ESSAY", 1, List.of(), expectedAnswer);
    }

    private List<CreateQuestionRequestDto> parseReviewData(String data) {
        if (data == null || data.isBlank()) return List.of();

        List<CreateQuestionRequestDto> questions = new ArrayList<>();

        String[] entries = data.split(";");

        for (String entry : entries) {
            String[] parts = entry.split("\\|");
            if (parts.length < 2) continue;

            String type = parts[0].trim();

            switch (type) {
                case "MCQ" -> questions.add(parseMCQ(parts));
                case "TF" -> questions.add(parseTF(parts));
                case "ESS" -> questions.add(parseESS(parts));
                default -> {
                } // skip unknown types
            }
        }

        return questions;
    }

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################
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
    public CreateExamFromReviewRequestDto generateExamFromReview(List<CardContent> cards) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("cards", cards);

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_EXAM_FROM_FORGOTTEN_FLASHCARD,
                    body,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(raw);

            return CreateExamFromReviewRequestDto.builder()
                    .title(root.get("title").asText())
                    .description(root.get("description").asText())
                    .duration(root.get("duration").asLong())
                    .questions(parseReviewData(root.get("data").asText()))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Exam from Review", e);
        }
    }

    @Override
    public CreateExamFromReviewRequestDto generateExamFromQuestions(List<QuestionContent> questions) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("questions", questions);

            String raw = restHttpClientUtil.post(
                    aiServiceBaseApi + GENERATE_EXAM_FROM_WRONG_ANSWERS,
                    body,
                    String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode root = objectMapper.readTree(raw);

            return CreateExamFromReviewRequestDto.builder()
                    .title(root.get("title").asText())
                    .description(root.get("description").asText())
                    .duration(root.get("duration").asLong())
                    .questions(parseReviewData(root.get("data").asText()))
                    .build();

        } catch (Exception e) {
            throw new RuntimeException("Failed to call AI service for generating Exam from Questions", e);
        }
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