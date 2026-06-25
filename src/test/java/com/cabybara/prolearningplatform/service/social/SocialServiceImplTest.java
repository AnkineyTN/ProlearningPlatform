package com.cabybara.prolearningplatform.service.social;

import com.cabybara.prolearningplatform.dto.helper.ResourceViewCountProjection;
import com.cabybara.prolearningplatform.dto.helper.Social.SocialNoteProjection;
import com.cabybara.prolearningplatform.dto.response.social.TopCreatorResponseDto;
import com.cabybara.prolearningplatform.dto.response.social.TrendingResourceResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.ResourceType;
import com.cabybara.prolearningplatform.enums.TrendingPeriod;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.repository.ActivityLogRepository;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.ResourceViewLogRepository;
import com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.social.impl.SocialServiceImpl;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ActivityLogRepository activityLogRepository;

    @Mock
    private ResourceViewLogRepository resourceViewLogRepository;

    @Mock
    private CardItemRepository cardItemRepository;

    @Mock
    private UserFavoriteResourceRepository userFavoriteResourceRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @Test
    void getSocialNotesReturnsOnlyPublic() {
        SocialServiceImpl service = new SocialServiceImpl(
                noteRepository, flashcardRepository, examRepository,
                userRepository, activityLogRepository, resourceViewLogRepository,
                cardItemRepository, userFavoriteResourceRepository, authenticationContext);

        Pageable pageable = PageRequest.of(0, 10);

        SocialNoteProjection sn1 = mock(SocialNoteProjection.class);
        when(sn1.getId()).thenReturn(1L);
        when(sn1.getSetId()).thenReturn(1L);
        when(sn1.getTitle()).thenReturn("Note 1");
        when(sn1.getDescription()).thenReturn("desc");
        when(sn1.getCreatedAt()).thenReturn(Timestamp.from(Instant.now()));
        when(sn1.getUpdatedAt()).thenReturn(Timestamp.from(Instant.now()));
        when(sn1.getOwnerId()).thenReturn(1L);
        when(sn1.getOwnerFirstName()).thenReturn("John");
        when(sn1.getOwnerLastName()).thenReturn("Doe");
        when(sn1.getOwnerAvatarUrl()).thenReturn(null);

        SocialNoteProjection sn2 = mock(SocialNoteProjection.class);
        when(sn2.getId()).thenReturn(2L);
        when(sn2.getSetId()).thenReturn(2L);
        when(sn2.getTitle()).thenReturn("Note 2");
        when(sn2.getDescription()).thenReturn("desc 2");
        when(sn2.getCreatedAt()).thenReturn(Timestamp.from(Instant.now()));
        when(sn2.getUpdatedAt()).thenReturn(Timestamp.from(Instant.now()));
        when(sn2.getOwnerId()).thenReturn(2L);
        when(sn2.getOwnerFirstName()).thenReturn("Jane");
        when(sn2.getOwnerLastName()).thenReturn("Smith");
        when(sn2.getOwnerAvatarUrl()).thenReturn(null);

        SocialNoteProjection sn3 = mock(SocialNoteProjection.class);
        when(sn3.getId()).thenReturn(3L);
        when(sn3.getSetId()).thenReturn(3L);
        when(sn3.getTitle()).thenReturn("Note 3");
        when(sn3.getDescription()).thenReturn("desc 3");
        when(sn3.getCreatedAt()).thenReturn(Timestamp.from(Instant.now()));
        when(sn3.getUpdatedAt()).thenReturn(Timestamp.from(Instant.now()));
        when(sn3.getOwnerId()).thenReturn(3L);
        when(sn3.getOwnerFirstName()).thenReturn("Bob");
        when(sn3.getOwnerLastName()).thenReturn("Brown");
        when(sn3.getOwnerAvatarUrl()).thenReturn(null);

        Page<SocialNoteProjection> page = new PageImpl<>(
                List.of(sn1, sn2, sn3), pageable, 3);

        when(noteRepository.findSocialNotes(eq(""), eq(pageable))).thenReturn(page);

        var result = service.getSocialNotes("", pageable);

        assertEquals(3, result.getContent().size());
    }

    @Test
    void getTrendingResourcesReturnsOrderedList() {
        SocialServiceImpl service = new SocialServiceImpl(
                noteRepository, flashcardRepository, examRepository,
                userRepository, activityLogRepository, resourceViewLogRepository,
                cardItemRepository, userFavoriteResourceRepository, authenticationContext);

        Note note = Note.builder()
                .title("Trending Note")
                .description("trending desc")
                .build();
        note.setId(1L);

        ResourceViewCountProjection viewRow = mock(ResourceViewCountProjection.class);
        when(viewRow.getResourceId()).thenReturn(1L);
        when(viewRow.getResourceType()).thenReturn("NOTE");
        when(viewRow.getViewCount()).thenReturn(10L);

        when(resourceViewLogRepository.findTopResourcesByViewsAndType(
                eq(ContentType.NOTE), any(OffsetDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(viewRow));
        when(activityLogRepository.findTopResourcesBySessions(
                any(LocalDate.class), anyList(), any(Pageable.class)))
                .thenReturn(List.of());
        when(noteRepository.findById(1L)).thenReturn(Optional.of(note));

        var result = service.getTrendingResources(TrendingPeriod.D7, 10, ResourceType.NOTE);

        assertFalse(result.isEmpty());
        assertEquals("Trending Note", result.get(0).title());
    }

    @Test
    void getTopCreatorsReturnsTopN() {
        SocialServiceImpl service = new SocialServiceImpl(
                noteRepository, flashcardRepository, examRepository,
                userRepository, activityLogRepository, resourceViewLogRepository,
                cardItemRepository, userFavoriteResourceRepository, authenticationContext);

        List<Object[]> creators = List.of(
                new Object[]{1L, "Alice", "Smith", "alice@test.com", "url1", 10L, 3L},
                new Object[]{2L, "Bob", "Jones", "bob@test.com", "url2", 8L, 2L},
                new Object[]{3L, "Charlie", "Brown", "charlie@test.com", "url3", 6L, 1L},
                new Object[]{4L, "Diana", "Prince", "diana@test.com", "url4", 4L, 0L},
                new Object[]{5L, "Eve", "Adams", "eve@test.com", "url5", 2L, 0L}
        );

        when(userRepository.findTopCreators(any(OffsetDateTime.class), any(Pageable.class)))
                .thenReturn(creators);

        var result = service.getTopCreators(TrendingPeriod.D7, 5);

        assertEquals(5, result.size());
        assertEquals("Alice Smith", result.get(0).fullName());
    }
}
