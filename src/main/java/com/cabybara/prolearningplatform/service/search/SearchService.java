package com.cabybara.prolearningplatform.service.search;

import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;

import java.util.List;

public interface SearchService {
    List<SearchResponseDto> searchForUser(String keyword, int limit);
}
