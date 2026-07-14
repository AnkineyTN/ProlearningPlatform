package com.cabybara.prolearningplatform.service.search;

import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.enums.SearchType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SearchService {
    Page<SearchResponseDto> search(String keyword, SearchType searchType, Pageable pageable);

    Page<SearchResponseDto> searchForCurrentUser(String keyword, SearchType searchType, Pageable pageable);
}
