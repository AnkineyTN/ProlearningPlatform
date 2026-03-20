package com.cabybara.prolearningplatform.service.search.impl;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.mapper.SearchMapper;
import com.cabybara.prolearningplatform.repository.SearchIndexRepository;
import com.cabybara.prolearningplatform.service.search.SearchService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {
    private final AuthenticationContext authenticationContext;
    private final SearchIndexRepository searchIndexRepository;
    private final SearchMapper searchMapper;


    @Override
    public List<SearchResponseDto> searchForUser(String keyword, int limit) {
        Long userId = authenticationContext.getCurrentUserId();

        List<SearchResultDto> searchResultDtos = searchIndexRepository.searchByUserId(keyword, userId, limit);

        return searchResultDtos.stream()
                .map(searchMapper::toSearchResponseDto)
                .toList();
    }
}
