package com.cabybara.prolearningplatform.service.search;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.SearchType;
import com.cabybara.prolearningplatform.mapper.SearchMapper;
import com.cabybara.prolearningplatform.repository.SearchIndexRepository;
import com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository;
import com.cabybara.prolearningplatform.service.search.impl.SearchServiceImpl;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceImplTest {

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private SearchIndexRepository searchIndexRepository;

    @Mock
    private SearchMapper searchMapper;

    @Mock
    private UserFavoriteResourceRepository userFavoriteResourceRepository;

    @Test
    void searchByKeywordReturnsResults() {
        SearchServiceImpl service = new SearchServiceImpl(
                authenticationContext, searchIndexRepository, searchMapper, userFavoriteResourceRepository);

        SearchResultDto mockDto = mock(SearchResultDto.class);
        SearchResponseDto responseDto = new SearchResponseDto(1L, 1L, "Java Notes", "desc", "NOTE", 1L, false);

        Page<SearchResultDto> mockPage = new PageImpl<>(List.of(mockDto));
        when(searchIndexRepository.search(eq("java"), isNull(), eq("NOTE"), any()))
                .thenReturn(mockPage);
        when(searchMapper.toSearchResponseDto(any(SearchResultDto.class))).thenReturn(responseDto);
        when(authenticationContext.getCurrentUserId()).thenReturn(null);

        var result = service.search("java", SearchType.NOTE, PageRequest.of(0, 10));

        assertEquals(1, result.getTotalElements());
        assertEquals("Java Notes", result.getContent().get(0).title());
    }

    @Test
    void searchWithEmptyKeywordReturnsEmpty() {
        SearchServiceImpl service = new SearchServiceImpl(
                authenticationContext, searchIndexRepository, searchMapper, userFavoriteResourceRepository);

        Page<SearchResultDto> emptyPage = new PageImpl<>(List.of());
        when(searchIndexRepository.search(eq(""), isNull(), eq("NOTE"), any()))
                .thenReturn(emptyPage);
        when(authenticationContext.getCurrentUserId()).thenReturn(null);

        var result = service.search("", SearchType.NOTE, PageRequest.of(0, 10));

        assertTrue(result.isEmpty());
    }

    @Test
    void searchForCurrentUserFiltersByOwnership() {
        SearchServiceImpl service = new SearchServiceImpl(
                authenticationContext, searchIndexRepository, searchMapper, userFavoriteResourceRepository);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        SearchResultDto mockDto = mock(SearchResultDto.class);
        when(mockDto.getId()).thenReturn(1L);
        when(mockDto.getType()).thenReturn("NOTE");

        Page<SearchResultDto> mockPage = new PageImpl<>(List.of(mockDto));
        when(searchIndexRepository.search(eq("java"), eq(1L), eq("NOTE"), any()))
                .thenReturn(mockPage);

        SearchResponseDto responseDto = new SearchResponseDto(1L, 1L, "My Note", "desc", "NOTE", 1L, false);
        when(searchMapper.toSearchResponseDto(any(SearchResultDto.class))).thenReturn(responseDto);
        when(userFavoriteResourceRepository.findFavoritedResourceIds(
                eq(1L), argThat(list -> list.contains(1L)), eq(ContentType.NOTE)))
                .thenReturn(Collections.emptySet());

        var result = service.searchForCurrentUser("java", SearchType.NOTE, PageRequest.of(0, 5));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
