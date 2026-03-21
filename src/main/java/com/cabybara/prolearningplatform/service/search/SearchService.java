package com.cabybara.prolearningplatform.service.search;

import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.enums.SearchType;

import java.util.List;

public interface SearchService {
    List<SearchResponseDto> search(String keyword, SearchType searchType, int limit);

    List<SearchResponseDto> searchForCurrentUser(String keyword, SearchType searchType, int limit);
}
