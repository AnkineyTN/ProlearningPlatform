package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.enums.AI.RoadmapLevel;
import com.cabybara.prolearningplatform.enums.LlmProvider;
import com.cabybara.prolearningplatform.service.ai.impl.AIRoadmapServiceImpl;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
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
class AIRoadmapServiceImplTest {

    private static final DecryptedLlmConfig CFG = new DecryptedLlmConfig(LlmProvider.OPENAI, "gpt-4o-mini", "sk-test");

    @Mock
    private AIServiceClient aiServiceClient;

    @Mock
    private UserLlmConfigService userLlmConfigService;

    @Test
    void generateRoadmapReturnsPreview() {
        AIRoadmapServiceImpl service = new AIRoadmapServiceImpl(aiServiceClient, new ObjectMapper(), userLlmConfigService);

        RoadmapPreviewRequestDto request = new RoadmapPreviewRequestDto();
        request.setGoal("Java");
        request.setLevel(RoadmapLevel.BEGINNER);

        when(userLlmConfigService.getDecryptedConfigForCurrentUser()).thenReturn(CFG);
        when(aiServiceClient.postForGeneration(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"roadmap_title\":\"Learn Java\",\"overview\":\"A roadmap to master Java fundamentals\",\"estimated_total_hours\":20,\"chapters\":[{\"chapter_id\":\"ch1\",\"chapter_title\":\"Introduction\",\"objective\":\"Get started\",\"topics\":[{\"topic_id\":\"t1\",\"topic_title\":\"Setup\",\"description\":\"Install JDK and IDE\"}]}]}");

        RoadmapPreviewResponseDto response = service.generateRoadmap(request, List.of());

        assertNotNull(response);
        assertNotNull(response.getChapters());
    }
}
