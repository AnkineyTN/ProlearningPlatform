package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.dto.request.note.ExplainNoteRequestDTO;
import com.cabybara.prolearningplatform.dto.request.note.SummarizeFileRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.ExplainNoteResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.SummarizeFileResponseDTO;
import com.cabybara.prolearningplatform.enums.LlmProvider;
import com.cabybara.prolearningplatform.service.ai.impl.AINoteServiceImpl;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AINoteServiceImplTest {

    private static final DecryptedLlmConfig CFG = new DecryptedLlmConfig(LlmProvider.OPENAI, "gpt-4o-mini", "sk-test");

    @Mock
    private AIServiceClient aiServiceClient;

    @Mock
    private UserLlmConfigService userLlmConfigService;

    @Test
    void explainNoteReturnsExplanation() {
        AINoteServiceImpl service = new AINoteServiceImpl(aiServiceClient, new ObjectMapper(), userLlmConfigService);

        ExplainNoteRequestDTO request = new ExplainNoteRequestDTO();
        request.setNoteId(1L);
        request.setQueryText("What is inheritance?");

        when(userLlmConfigService.getDecryptedConfigForCurrentUser()).thenReturn(CFG);
        when(aiServiceClient.postForGeneration(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"data\": \"Inheritance is a mechanism where one class acquires properties of another\"}");

        ExplainNoteResponseDTO response = service.explainNote(request);

        assertNotNull(response);
        assertNotNull(response.getAnswer());
    }

    @Test
    void summarizeFileReturnsSummary() {
        AINoteServiceImpl service = new AINoteServiceImpl(aiServiceClient, new ObjectMapper(), userLlmConfigService);

        SummarizeFileRequestDTO request = new SummarizeFileRequestDTO();
        request.setFileUrl("https://example.com/file.pdf");

        when(userLlmConfigService.getDecryptedConfigForCurrentUser()).thenReturn(CFG);
        when(aiServiceClient.postForGeneration(anyString(), any(), any(), eq(String.class)))
                .thenReturn("{\"data\": \"This file covers Java fundamentals including OOP concepts, collections, and streams\"}");

        SummarizeFileResponseDTO response = service.summarizeFile(request);

        assertNotNull(response);
        assertNotNull(response.getSummary());
    }
}
