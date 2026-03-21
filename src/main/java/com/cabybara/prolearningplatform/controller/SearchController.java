package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.service.search.SearchService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search")
@Validated
@Slf4j
public class SearchController {
    private final SearchService searchService;

    @GetMapping()
    @Operation(
            summary = "Search all resource (note, set, flashcard, exam) belongs to the current user"
    )
    public ResponseEntity<ApiResponse<?>> searchAllForUser(
            @RequestParam String keyword,
            @RequestParam int limit,
            @ParameterObject @PageableDefault(page = 0, size = 6, sort = "id") Pageable pageable
    ) {
        List<SearchResponseDto> searchResponseDtos = searchService.searchForUser(keyword, limit);

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
                .body(ResponseUtil.success("Successfully", pagedResponse, PaginationResponseDto.builder()
                        .pageSize(pageSize)
                        .currentPage(currentPage)
                        .totalItems(totalItems)
                        .totalPages(totalPages)
                        .build()
                ));
    }
}
