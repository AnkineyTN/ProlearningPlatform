package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.knowledge.KnowledgeAIResult;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.service.ai.impl.AIKnowledgeServiceImpl;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIKnowledgeServiceImplTest {

    @Mock
    private RestHttpClientUtil restHttpClientUtil;

    @Test
    void analyzeKnowledgeReturnsTopics() {
        AIKnowledgeServiceImpl service = new AIKnowledgeServiceImpl(restHttpClientUtil, new ObjectMapper());

        List<TopicAccuracyDto> topicAccuracies = List.of(
                new TopicAccuracyDto("OOP", 0.85),
                new TopicAccuracyDto("Collections", 0.60)
        );

        when(restHttpClientUtil.post(anyString(), any(), eq(String.class)))
                .thenReturn("{\"data\": {\"strengths\": \"Good at OOP\", \"weaknesses\": \"Collections\", \"improvements\": \"Practice with Streams\"}}");

        KnowledgeAIResult result = service.analyzeKnowledge(topicAccuracies);

        assertNotNull(result);
    }
}
