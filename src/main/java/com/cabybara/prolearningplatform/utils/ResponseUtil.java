package com.cabybara.prolearningplatform.utils;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseUtil {

    public static <T> ApiResponse<T> success(String message, T data, Object metadata) {
        return new ApiResponse<>("success", message, data, metadata);
    }

    public static <T> ApiResponse<T> error(String message, T data, Object metadata) {
        return new ApiResponse<>("error", message, data, metadata);
    }

    public static ResponseEntity<ApiResponse<?>> toPaginationedResponse(String message, Pageable pageable, List<SearchResponseDto> searchResponseDtos) {
        int totalItems = searchResponseDtos.size();
        int pageSize = pageable.getPageSize();
        int currentPage = pageable.getPageNumber();

        int start = currentPage * pageSize;
        int end = Math.min(start + pageSize, totalItems);

        List<SearchResponseDto> pagedResponse =
                start >= totalItems ? List.of() : searchResponseDtos.subList(start, end);

        int totalPages = (int) Math.ceil((double) totalItems / pageSize);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success(message, pagedResponse, PaginationResponseDto.builder()
                        .pageSize(pageSize)
                        .currentPage(currentPage)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .build()
                ));
    }
}
