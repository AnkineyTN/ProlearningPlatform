package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.service.search.SearchService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public ResponseEntity<ApiResponse<?>> searchAllForUser(@RequestParam String keyword, @RequestParam int limit) {
        List<SearchResponseDto> searchResponseDtos = searchService.searchForUser(keyword, limit);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", searchResponseDtos, null));
    }
}
