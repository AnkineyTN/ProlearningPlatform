package com.cabybara.prolearningplatform.service.ai.impl;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.GenerateExamByWebRequestDto;
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
    private static final String GENERATE_EXAM_BY_WEB_PATH  = "/tests/from-web";

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
    public GenerateExamByAIResponseDto generateExamByNotes(List<String> contents, Map<String, Integer> questions, String freeText, Language language) {
        try {
            Map<String, Object> body = new HashMap<>();
            body.put("contents", contents);
            body.put("questions", questions);
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
}
