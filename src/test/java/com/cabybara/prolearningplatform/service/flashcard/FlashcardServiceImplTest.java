package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.flashcard.impl.FlashcardServiceImpl;
import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentAsyncService;
import com.cabybara.prolearningplatform.service.permission.impl.FlashcardPermissionService;
import com.cabybara.prolearningplatform.service.set.SetService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.apache.coyote.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlashcardServiceImplTest {

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserService userService;

    @Mock
    private SetService setService;

    @Mock
    private FlashcardMapper flashcardMapper;

    @Mock
    private AssetService assetService;

    @Mock
    private CardItemMapper cardItemMapper;

    @Mock
    private SetRepository setRepository;

    @Mock
    private AIFlashcardService aiFlashcardService;

    @Mock
    private FlashcardPermissionService flashcardPermissionService;

    @Mock
    private TopicAssignmentAsyncService topicAssignmentAsyncService;

    @Mock
    private UserFavoriteResourceRepository userFavoriteResourceRepository;

    @Test
    void createFlashcardWithValidData() throws BadRequestException {
        FlashcardServiceImpl service = new FlashcardServiceImpl(
                authenticationContext, flashcardRepository, noteRepository,
                userService, setService, flashcardMapper, assetService,
                cardItemMapper, setRepository, aiFlashcardService,
                flashcardPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository
        );

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        FlashcardCreateRequestDto request = new FlashcardCreateRequestDto();
        request.setTitle("My Flashcard");
        request.setDescription("desc");
        request.setPrivacy(Privacy.PRIVATE.name());

        Flashcard flashcard = new Flashcard();
        flashcard.setTitle("My Flashcard");
        flashcard.setDescription("desc");
        flashcard.setPrivacy(Privacy.PRIVATE);

        Flashcard savedFlashcard = new Flashcard();
        savedFlashcard.setId(1L);
        savedFlashcard.setTitle("My Flashcard");
        savedFlashcard.setDescription("desc");
        savedFlashcard.setPrivacy(Privacy.PRIVATE);
        savedFlashcard.setSet(set);
        savedFlashcard.setUser(user);

        FlashcardResponseDto responseDto = new FlashcardResponseDto();

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(setService.getSetById(1L)).thenReturn(set);
        when(flashcardRepository.existsBySetIdAndTitle(1L, "My Flashcard")).thenReturn(false);
        when(flashcardMapper.toFlashcard(request)).thenReturn(flashcard);
        when(flashcardRepository.save(captor.capture())).thenReturn(savedFlashcard);
        when(flashcardMapper.toFlashcardResponseDto(savedFlashcard)).thenReturn(responseDto);

        FlashcardResponseDto result = service.addFlashcardManual(1L, request);

        Flashcard saved = captor.getValue();
        assertEquals("My Flashcard", saved.getTitle());
        assertEquals("desc", saved.getDescription());
        assertEquals(Privacy.PRIVATE, saved.getPrivacy());
        assertSame(responseDto, result);
        verify(flashcardRepository).save(any(Flashcard.class));
    }

    @Test
    void createFlashcardWithDuplicateTitle() throws BadRequestException {
        FlashcardServiceImpl service = new FlashcardServiceImpl(
                authenticationContext, flashcardRepository, noteRepository,
                userService, setService, flashcardMapper, assetService,
                cardItemMapper, setRepository, aiFlashcardService,
                flashcardPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository
        );

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        FlashcardCreateRequestDto request = new FlashcardCreateRequestDto();
        request.setTitle("dup");
        request.setDescription("desc");
        request.setPrivacy(Privacy.PRIVATE.name());

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);
        when(setService.getSetById(1L)).thenReturn(set);
        when(flashcardRepository.existsBySetIdAndTitle(1L, "dup")).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> {
            service.addFlashcardManual(1L, request);
        });

        verify(flashcardRepository, never()).save(any());
    }

    @Test
    void deleteFlashcardAsOwner() throws BadRequestException {
        FlashcardServiceImpl service = new FlashcardServiceImpl(
                authenticationContext, flashcardRepository, noteRepository,
                userService, setService, flashcardMapper, assetService,
                cardItemMapper, setRepository, aiFlashcardService,
                flashcardPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository
        );

        User user = TestFixtures.user(1L);
        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setUser(user);

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardRepository.deleteByIdAndSetIdAndSetUserId(1L, 1L, 1L)).thenReturn(1);

        service.deleteFlashcard(1L, 1L);

        verify(flashcardRepository).deleteByIdAndSetIdAndSetUserId(1L, 1L, 1L);
    }

    @Test
    void deleteFlashcardAsNonOwner() {
        FlashcardServiceImpl service = new FlashcardServiceImpl(
                authenticationContext, flashcardRepository, noteRepository,
                userService, setService, flashcardMapper, assetService,
                cardItemMapper, setRepository, aiFlashcardService,
                flashcardPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository
        );

        User otherUser = TestFixtures.user(2L);
        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setUser(otherUser);

        when(flashcardRepository.findById(1L)).thenReturn(Optional.of(flashcard));
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardRepository.deleteByIdAndSetIdAndSetUserId(1L, 1L, 1L)).thenReturn(0);

        assertThrows(BadRequestException.class, () -> {
            service.deleteFlashcard(1L, 1L);
        });
    }

    @Test
    void updateFlashcardWithValidData() throws BadRequestException {
        FlashcardServiceImpl service = new FlashcardServiceImpl(
                authenticationContext, flashcardRepository, noteRepository,
                userService, setService, flashcardMapper, assetService,
                cardItemMapper, setRepository, aiFlashcardService,
                flashcardPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository
        );

        User user = TestFixtures.user(1L);
        Flashcard existingFlashcard = new Flashcard();
        existingFlashcard.setId(1L);
        existingFlashcard.setTitle("old title");
        existingFlashcard.setUser(user);

        FlashcardUpdatingRequestDto request = new FlashcardUpdatingRequestDto();
        request.setTitle("new title");

        FlashcardResponseDto responseDto = new FlashcardResponseDto();

        ArgumentCaptor<Flashcard> captor = ArgumentCaptor.forClass(Flashcard.class);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardRepository.getFlashcardByIdAndSetIdAndSetUserId(1L, 1L, 1L))
                .thenReturn(Optional.of(existingFlashcard));
        doAnswer(invocation -> {
            FlashcardUpdatingRequestDto dto = invocation.getArgument(0);
            Flashcard target = invocation.getArgument(1);
            target.setTitle(dto.getTitle());
            return null;
        }).when(flashcardMapper).updateFlashcardFromDto(eq(request), eq(existingFlashcard));
        when(flashcardRepository.save(captor.capture())).thenReturn(existingFlashcard);
        when(flashcardMapper.toFlashcardResponseDto(existingFlashcard)).thenReturn(responseDto);

        FlashcardResponseDto result = service.updateFlashcard(1L, 1L, request);

        verify(flashcardMapper).updateFlashcardFromDto(request, existingFlashcard);
        assertEquals("new title", existingFlashcard.getTitle());
        verify(flashcardRepository).save(existingFlashcard);
        assertEquals("new title", captor.getValue().getTitle());
        assertSame(responseDto, result);
    }

    @Test
    void getDetailFlashcardReturnsWithMemberRoleAndFavoriteStatus() {
        FlashcardServiceImpl service = new FlashcardServiceImpl(
                authenticationContext, flashcardRepository, noteRepository,
                userService, setService, flashcardMapper, assetService,
                cardItemMapper, setRepository, aiFlashcardService,
                flashcardPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository
        );

        User user = TestFixtures.user(1L);
        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setTitle("My Flashcard");
        flashcard.setUser(user);

        DetailFlashcardResponseDto responseDto = new DetailFlashcardResponseDto();

        when(flashcardRepository.findByIdAndSetId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardPermissionService.getUserRoleInFlashcard(1L, 1L)).thenReturn(NoteRole.OWNER);
        when(userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(1L, 1L, ContentType.FLASHCARD))
                .thenReturn(true);
        when(flashcardMapper.toDetailFlashcardResponseDto(flashcard)).thenReturn(responseDto);

        DetailFlashcardResponseDto result = service.getDetailFlashcard(1L, 1L);

        assertEquals(NoteRole.OWNER, result.getUserRole());
        assertTrue(result.getIsFavorited());
    }
}
