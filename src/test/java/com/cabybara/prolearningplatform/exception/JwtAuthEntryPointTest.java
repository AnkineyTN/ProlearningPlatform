package com.cabybara.prolearningplatform.exception;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtAuthEntryPointTest {

    private final JwtAuthEntryPoint entryPoint = new JwtAuthEntryPoint();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void commenceWritesUnauthorizedApiResponse() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI("/api/secure");
        MockHttpServletResponse response = new MockHttpServletResponse();

        entryPoint.commence(request, response, new BadCredentialsException("Bad credentials"));

        JsonNode json = objectMapper.readTree(response.getContentAsByteArray());
        assertEquals(401, response.getStatus());
        assertEquals(MediaType.APPLICATION_JSON_VALUE, response.getContentType());
        assertEquals("error", json.get("status").asText());
        assertEquals("Unauthorized", json.get("message").asText());
        assertEquals("Bad credentials", json.get("data").asText());
        assertEquals("path: /api/secure", json.get("metadata").asText());
    }
}
