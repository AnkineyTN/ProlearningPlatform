package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class ResponseUtilTest {

    @Test
    void successBuildsExpectedEnvelope() {
        ApiResponse<String> response = ResponseUtil.success("ok", "payload", "meta");

        assertEquals("success", response.getStatus());
        assertEquals("ok", response.getMessage());
        assertEquals("payload", response.getData());
        assertEquals("meta", response.getMetadata());
    }

    @Test
    void errorWithCodeBuildsErrorMetadata() {
        ApiResponse<Object> response = ResponseUtil.error("failed", null, "BAD_REQUEST", "/api/test");

        assertEquals("error", response.getStatus());
        assertEquals("failed", response.getMessage());
        assertEquals(null, response.getData());
        assertInstanceOf(ErrorMetadata.class, response.getMetadata());
        ErrorMetadata metadata = (ErrorMetadata) response.getMetadata();
        assertEquals("BAD_REQUEST", metadata.getCode());
        assertEquals("/api/test", metadata.getPath());
    }

    @Test
    void toPaginationedResponseSlicesDataAndBuildsMetadata() {
        List<SearchResponseDto> items = List.of(
                new SearchResponseDto(1L, 10L, "one", "desc-1", "todo", 100L),
                new SearchResponseDto(2L, 20L, "two", "desc-2", "todo", 100L),
                new SearchResponseDto(3L, 30L, "three", "desc-3", "todo", 100L)
        );

        ResponseEntity<ApiResponse<?>> response = ResponseUtil.toPaginationedResponse(
                "paged",
                PageRequest.of(1, 2),
                items
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        ApiResponse<?> body = response.getBody();
        assertEquals("success", body.getStatus());
        assertEquals("paged", body.getMessage());
        assertEquals(List.of(items.get(2)), body.getData());
        assertInstanceOf(PaginationResponseDto.class, body.getMetadata());

        PaginationResponseDto metadata = (PaginationResponseDto) body.getMetadata();
        assertEquals(1, metadata.getCurrentPage());
        assertEquals(2, metadata.getPageSize());
        assertEquals(3, metadata.getTotalItems());
        assertEquals(2, metadata.getTotalPages());
    }

    @Test
    void toPaginationedResponseReturnsEmptyListWhenPageOutOfRange() {
        ResponseEntity<ApiResponse<?>> response = ResponseUtil.toPaginationedResponse(
                "paged",
                PageRequest.of(3, 2),
                List.of(new SearchResponseDto(1L, 10L, "one", "desc-1", "todo", 100L))
        );

        ApiResponse<?> body = response.getBody();
        assertEquals(List.of(), body.getData());

        PaginationResponseDto metadata = (PaginationResponseDto) body.getMetadata();
        assertEquals(3, metadata.getCurrentPage());
        assertEquals(1, metadata.getTotalItems());
        assertEquals(1, metadata.getTotalPages());
    }
}
