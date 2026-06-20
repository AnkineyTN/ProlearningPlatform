package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.request.exam.EssayGradingRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.ExplainWrongAnswerRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.EssayGradingResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExplainWrongAnswerResponseDto;
import com.cabybara.prolearningplatform.service.ai.impl.AIExamServiceImpl;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIExamServiceImplTest {

    @Mock
    private RestHttpClientUtil restHttpClientUtil;

    @Test
    void gradeEssayReturnsScore() {
        AIExamServiceImpl service = new AIExamServiceImpl(restHttpClientUtil, new ObjectMapper());

        EssayGradingRequestDto request = new EssayGradingRequestDto(
                1L, 100L, "What is Java?", "A language", "A programming language", 10);

        when(restHttpClientUtil.post(anyString(), any(), eq(String.class)))
                .thenReturn("{\"score\": 8.5, \"feedback\": \"Good answer\"}");

        EssayGradingResponseDto response = service.gradeEssay(request);

        assertEquals(8.5, response.score());
        assertNotNull(response.feedback());
    }

    @Test
    void explainWrongAnswerReturnsExplanation() {
        AIExamServiceImpl service = new AIExamServiceImpl(restHttpClientUtil, new ObjectMapper());

        ExplainWrongAnswerRequestDto request = new ExplainWrongAnswerRequestDto(
                "What is polymorphism?", "Multiple forms", "Inheritance", null);

        when(restHttpClientUtil.post(anyString(), any(), eq(String.class)))
                .thenReturn("{\"explanation\": \"Polymorphism allows objects of different types to be treated as a common type\"}");

        ExplainWrongAnswerResponseDto response = service.explainWrongAnswer(request);

        assertNotNull(response);
        assertNotNull(response.explanation());
    }
}
