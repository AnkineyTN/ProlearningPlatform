package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.DecryptedLlmConfig;
import com.cabybara.prolearningplatform.exception.AIServiceException;
import com.cabybara.prolearningplatform.exception.LlmNotConfiguredException;
import com.cabybara.prolearningplatform.utils.RestHttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.ResourceAccessException;

/**
 * Single entry point for calling the external AI Service. Centralizes:
 * <ul>
 *   <li>base URL composition,</li>
 *   <li>the {@code X-Internal-Api-Key} header on every call,</li>
 *   <li>the per-user BYOK headers ({@code X-LLM-Provider/Model/Api-Key}) on generation calls,</li>
 *   <li>mapping AI Service / provider errors to friendly, FE-safe exceptions.</li>
 * </ul>
 * Header values are never logged.
 */
@Component
@Slf4j
public class AIServiceClient {

    private static final String HEADER_INTERNAL_API_KEY = "X-Internal-Api-Key";
    private static final String HEADER_LLM_PROVIDER = "X-LLM-Provider";
    private static final String HEADER_LLM_MODEL = "X-LLM-Model";
    private static final String HEADER_LLM_API_KEY = "X-LLM-Api-Key";

    private final RestHttpClientUtil restHttpClientUtil;
    private final String baseApi;
    private final String internalApiKey;

    public AIServiceClient(RestHttpClientUtil restHttpClientUtil,
                           @Value("${aiservice.api}") String baseApi,
                           @Value("${aiservice.internal-api-key:}") String internalApiKey) {
        this.restHttpClientUtil = restHttpClientUtil;
        this.baseApi = baseApi;
        this.internalApiKey = internalApiKey;
    }

    // ---- Generation calls (internal key + per-user LLM headers) ----

    public <B, R> R postForGeneration(String path, B body, DecryptedLlmConfig cfg, Class<R> responseType) {
        try {
            return restHttpClientUtil.post(baseApi + path, body, generationHeaders(cfg), responseType);
        } catch (HttpStatusCodeException e) {
            throw mapError(e);
        } catch (ResourceAccessException e) {
            throw connectionError(e);
        }
    }

    public <R> R postMultipartForGeneration(String path, MultiValueMap<String, Object> body,
                                            DecryptedLlmConfig cfg, Class<R> responseType) {
        try {
            return restHttpClientUtil.postMultipart(baseApi + path, body, generationHeaders(cfg), responseType);
        } catch (HttpStatusCodeException e) {
            throw mapError(e);
        } catch (ResourceAccessException e) {
            throw connectionError(e);
        }
    }

    // ---- Internal-only calls (embeddings / vector DB use the service's own key) ----

    public <B, R> R postInternalOnly(String path, B body, Class<R> responseType) {
        try {
            return restHttpClientUtil.post(baseApi + path, body, internalHeaders(), responseType);
        } catch (HttpStatusCodeException e) {
            throw mapError(e);
        } catch (ResourceAccessException e) {
            throw connectionError(e);
        }
    }

    public void deleteInternalOnly(String path) {
        try {
            restHttpClientUtil.delete(baseApi + path, internalHeaders());
        } catch (HttpStatusCodeException e) {
            throw mapError(e);
        } catch (ResourceAccessException e) {
            throw connectionError(e);
        }
    }

    // ---- Header assembly ----

    private HttpHeaders internalHeaders() {
        HttpHeaders headers = new HttpHeaders();
        if (internalApiKey != null && !internalApiKey.isBlank()) {
            headers.add(HEADER_INTERNAL_API_KEY, internalApiKey);
        }
        return headers;
    }

    private HttpHeaders generationHeaders(DecryptedLlmConfig cfg) {
        if (cfg == null || cfg.provider() == null
                || cfg.model() == null || cfg.model().isBlank()
                || cfg.apiKey() == null || cfg.apiKey().isBlank()) {
            throw new LlmNotConfiguredException();
        }
        HttpHeaders headers = internalHeaders();
        headers.add(HEADER_LLM_PROVIDER, cfg.provider().getWireValue());
        headers.add(HEADER_LLM_MODEL, cfg.model());
        headers.add(HEADER_LLM_API_KEY, cfg.apiKey());
        return headers;
    }

    // ---- Error mapping (never leak the upstream body or secrets) ----

    private RuntimeException mapError(HttpStatusCodeException e) {
        int status = e.getStatusCode().value();
        String body = safeBody(e);

        // Our own internal API key was rejected — a system misconfiguration, not the user's fault.
        if (status == 401) {
            log.error("AI Service rejected X-Internal-Api-Key (check aiservice.internal-api-key). status=401");
            return new AIServiceException(HttpStatus.BAD_GATEWAY,
                    "AI service authentication failed. Please contact support.");
        }

        if (status == 400) {
            if (body != null && body.toLowerCase().contains("missing required llm configuration")) {
                return new LlmNotConfiguredException();
            }
            return new AIServiceException(HttpStatus.BAD_REQUEST,
                    "The AI request was rejected. Please review your AI provider/model settings.");
        }

        // Provider key invalid / billing problem surfaced by the AI Service.
        if (status == 402 || status == 403) {
            return new AIServiceException(HttpStatus.BAD_REQUEST,
                    "Your AI provider rejected the request. Please check that your API key is valid and active.");
        }

        if (status == 429) {
            return new AIServiceException(HttpStatus.TOO_MANY_REQUESTS,
                    "Your AI provider is rate-limited or out of quota. Please try again later.");
        }

        log.error("AI Service call failed with status {}.", status);
        return new AIServiceException(HttpStatus.BAD_GATEWAY,
                "AI service is temporarily unavailable. Please try again later.");
    }

    private RuntimeException connectionError(ResourceAccessException e) {
        log.error("AI Service is unreachable: {}", e.getMessage());
        return new AIServiceException(HttpStatus.BAD_GATEWAY,
                "AI service is unreachable. Please try again later.");
    }

    private String safeBody(HttpStatusCodeException e) {
        try {
            return e.getResponseBodyAsString();
        } catch (Exception ignored) {
            return null;
        }
    }
}
