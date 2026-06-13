package com.cabybara.prolearningplatform.service.search.impl;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.enums.SearchType;
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
    private final com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository userFavoriteResourceRepository;

    private Long getCurrentUserIdSafe() {
        try {
            return authenticationContext.getCurrentUserId();
        } catch (Exception e) {
            return null;
        }
    }


    @Override
    public List<SearchResponseDto> search(String keyword, SearchType searchType, int limit) {
        String type = null;
        if (searchType != null) {
            type = searchType.name();
        }

        List<SearchResultDto> searchResultDtos = searchIndexRepository.search(keyword, null, type, limit);

        Long userId = getCurrentUserIdSafe();

        return searchResultDtos.stream()
                .map(dto -> {
                    SearchResponseDto result = searchMapper.toSearchResponseDto(dto);
                    boolean isFavorited = false;
                    if (userId != null && result.type() != null) {
                        try {
                            com.cabybara.prolearningplatform.enums.ContentType contentType = com.cabybara.prolearningplatform.enums.ContentType.valueOf(result.type());
                            isFavorited = userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, result.id(), contentType);
                        } catch (IllegalArgumentException e) {
                            // Ignore if not a valid content type (e.g. SET)
                        }
                    }
                    return new SearchResponseDto(result.id(), result.setId(), result.title(), result.description(), result.type(), result.userId(), isFavorited);
                })
                .toList();
    }

    @Override
    public List<SearchResponseDto> searchForCurrentUser(String keyword, SearchType searchType, int limit) {
        Long userId = authenticationContext.getCurrentUserId();

        String type = null;
        if (searchType != null) {
            type = searchType.name();
        }

        List<SearchResultDto> searchResultDtos = searchIndexRepository.search(keyword, userId, type, limit);

        return searchResultDtos.stream()
                .map(dto -> {
                    SearchResponseDto result = searchMapper.toSearchResponseDto(dto);
                    boolean isFavorited = false;
                    if (userId != null && result.type() != null) {
                        try {
                            com.cabybara.prolearningplatform.enums.ContentType contentType = com.cabybara.prolearningplatform.enums.ContentType.valueOf(result.type());
                            isFavorited = userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, result.id(), contentType);
                        } catch (IllegalArgumentException e) {
                            // Ignore if not a valid content type (e.g. SET)
                        }
                    }
                    return new SearchResponseDto(result.id(), result.setId(), result.title(), result.description(), result.type(), result.userId(), isFavorited);
                })
                .toList();
    }
}
