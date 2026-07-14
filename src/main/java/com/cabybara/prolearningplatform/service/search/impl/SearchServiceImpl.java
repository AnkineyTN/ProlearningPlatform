package com.cabybara.prolearningplatform.service.search.impl;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.SearchType;
import com.cabybara.prolearningplatform.mapper.SearchMapper;
import com.cabybara.prolearningplatform.repository.SearchIndexRepository;
import com.cabybara.prolearningplatform.service.search.SearchService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final AuthenticationContext authenticationContext;
    private final SearchIndexRepository searchIndexRepository;
    private final SearchMapper searchMapper;
    private final com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository userFavoriteResourceRepository;

    private Long getCurrentUserIdSafe() {
        try {
            return authenticationContext.getCurrentUserId();
        } catch (Exception e) {
            log.warn("Failed to get current user ID for search query", e);
            return null;
        }
    }


    @Override
    public Page<SearchResponseDto> search(String keyword, SearchType searchType, Pageable pageable) {
        String type = null;
        if (searchType != null) {
            type = searchType.name();
        }

        Page<SearchResultDto> searchResultPage = searchIndexRepository.search(keyword, null, type, pageable);

        Long userId = getCurrentUserIdSafe();
        Set<Long> favoritedIds = preloadFavoritedIds(userId, searchResultPage.getContent());

        return searchResultPage.map(dto -> {
            SearchResponseDto result = searchMapper.toSearchResponseDto(dto);
            boolean isFavorited = favoritedIds.contains(result.id());
            return new SearchResponseDto(result.id(), result.setId(), result.title(), result.description(), result.type(), result.userId(), isFavorited);
        });
    }

    @Override
    public Page<SearchResponseDto> searchForCurrentUser(String keyword, SearchType searchType, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();

        String type = null;
        if (searchType != null) {
            type = searchType.name();
        }

        Page<SearchResultDto> searchResultPage = searchIndexRepository.search(keyword, userId, type, pageable);

        Set<Long> favoritedIds = preloadFavoritedIds(userId, searchResultPage.getContent());

        return searchResultPage.map(dto -> {
            SearchResponseDto result = searchMapper.toSearchResponseDto(dto);
            boolean isFavorited = favoritedIds.contains(result.id());
            return new SearchResponseDto(result.id(), result.setId(), result.title(), result.description(), result.type(), result.userId(), isFavorited);
        });
    }

    private Set<Long> preloadFavoritedIds(Long userId, List<SearchResultDto> results) {
        if (userId == null) {
            return Collections.emptySet();
        }
        Map<ContentType, List<Long>> idsByType = results.stream()
                .filter(dto -> dto.getType() != null)
                .collect(Collectors.groupingBy(
                        dto -> ContentType.valueOf(dto.getType()),
                        Collectors.mapping(SearchResultDto::getId, Collectors.toList())
                ));
        Set<Long> allFavorited = new HashSet<>();
        for (var entry : idsByType.entrySet()) {
            allFavorited.addAll(userFavoriteResourceRepository.findFavoritedResourceIds(userId, entry.getValue(), entry.getKey()));
        }
        return allFavorited;
    }
}
