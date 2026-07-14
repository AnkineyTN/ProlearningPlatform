package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.enums.SearchType;
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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search")
@Validated
@Slf4j
public class SearchController {
    private final SearchService searchService;

    @GetMapping("")
    @Operation(
            summary = "Search resource that belongs to the all user"
    )
    public ResponseEntity<ApiResponse<?>> searchResourceByType(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "") SearchType searchType,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseUtil.toPaginationedResponse("Successfully", pageable, searchService.search(keyword, searchType, pageable));
    }

    @GetMapping("/me")
    @Operation(
            summary = "Search resource that only belongs to the current user"
    )
    public ResponseEntity<ApiResponse<?>> searchAllResourceForCurrentUser(
            @RequestParam String keyword,
            @RequestParam(required = false, defaultValue = "") SearchType searchType,
            @ParameterObject @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        return ResponseUtil.toPaginationedResponse("Successfully", pageable, searchService.searchForCurrentUser(keyword, searchType, pageable));
    }
}
