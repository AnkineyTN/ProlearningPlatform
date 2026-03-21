package com.cabybara.prolearningplatform.service.search;

import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;

import java.util.List;

public interface SearchService {
    List<SearchResponseDto> searchForCurrentUser(String keyword, int limit);

    List<SearchResponseDto> searchForAllUser(String keyword, int limit);
}
