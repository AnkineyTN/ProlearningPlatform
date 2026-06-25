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

        when(searchIndexRepository.search(eq("java"), isNull(), eq("NOTE"), eq(10)))
                .thenReturn(List.of(mockDto));
        when(searchMapper.toSearchResponseDto(any(SearchResultDto.class))).thenReturn(responseDto);

        var result = service.search("java", SearchType.NOTE, 10);

        assertEquals(1, result.size());
        assertEquals("Java Notes", result.get(0).title());
    }

    @Test
    void searchWithEmptyKeywordReturnsEmpty() {
        SearchServiceImpl service = new SearchServiceImpl(
                authenticationContext, searchIndexRepository, searchMapper, userFavoriteResourceRepository);

        when(searchIndexRepository.search(eq(""), isNull(), eq("NOTE"), eq(10)))
                .thenReturn(List.of());

        var result = service.search("", SearchType.NOTE, 10);

        assertTrue(result.isEmpty());
    }

    @Test
    void searchForCurrentUserFiltersByOwnership() {
        SearchServiceImpl service = new SearchServiceImpl(
                authenticationContext, searchIndexRepository, searchMapper, userFavoriteResourceRepository);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);

        SearchResultDto mockDto = mock(SearchResultDto.class);

        when(searchIndexRepository.search(eq("java"), eq(1L), eq("NOTE"), eq(5)))
                .thenReturn(List.of(mockDto));

        SearchResponseDto responseDto = new SearchResponseDto(1L, 1L, "My Note", "desc", "NOTE", 1L, false);
        when(searchMapper.toSearchResponseDto(any(SearchResultDto.class))).thenReturn(responseDto);
        when(userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(
                eq(1L), eq(1L), eq(ContentType.NOTE))).thenReturn(false);

        var result = service.searchForCurrentUser("java", SearchType.NOTE, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
    }
}
