package com.cabybara.prolearningplatform.service.exam;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamFromReviewRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateExamRequestDto;
import com.cabybara.prolearningplatform.dto.request.exam.CreateQuestionRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionListResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.ExamQuestionRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.ai.AIExamService;
import com.cabybara.prolearningplatform.service.exam.impl.ExamServiceImpl;
import com.cabybara.prolearningplatform.service.file.FileService;
import com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentAsyncService;
import com.cabybara.prolearningplatform.service.permission.impl.ExamPermissionService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ExamServiceImplTest {

    @Mock
    private FileService fileService;

    @Mock
    private AIExamService aiExamService;

    @Mock
    private QuestionService questionService;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private ExamRepository examRepository;

    @Mock
    private ExamQuestionRepository examQuestionRepository;

    @Mock
    private FlashcardRepository flashcardRepository;

    @Mock
    private CardItemRepository cardItemRepository;

    @Mock
    private SetRepository setRepository;

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private ExamMapper examMapper;

    @Mock
    private ExamPermissionService examPermissionService;

    @Mock
    private TopicAssignmentAsyncService topicAssignmentAsyncService;

    @Mock
    private UserFavoriteResourceRepository userFavoriteResourceRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    void createExamReturnsExamResponseDto() {
        ExamServiceImpl service = new ExamServiceImpl(
                fileService, aiExamService, questionService, authenticationContext,
                examRepository, examQuestionRepository, flashcardRepository,
                cardItemRepository, setRepository, noteRepository, examMapper,
                examPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository, userRepository
        );

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        CreateExamRequestDto request = CreateExamRequestDto.builder()
                .title("Test Exam")
                .description("desc")
                .privacy(Privacy.PRIVATE)
                .duration(60L)
                .build();

        Exam exam = new Exam();
        exam.setTitle("Test Exam");
        exam.setDescription("desc");
        exam.setPrivacy(Privacy.PRIVATE);

        Exam savedExam = new Exam();
        savedExam.setId(1L);
        savedExam.setTitle("Test Exam");
        savedExam.setDescription("desc");
        savedExam.setPrivacy(Privacy.PRIVATE);
        savedExam.setSet(set);
        savedExam.setCreatedBy(1L);

        ExamResponseDto responseDto = ExamResponseDto.builder()
                .id(1L)
                .title("Test Exam")
                .description("desc")
                .privacy(Privacy.PRIVATE)
                .duration(60L)
                .build();

        ArgumentCaptor<Exam> captor = ArgumentCaptor.forClass(Exam.class);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(setRepository.findById(1L)).thenReturn(Optional.of(set));
        when(examRepository.existsByTitleAndSet("Test Exam", set)).thenReturn(false);
        when(examMapper.toExam(request)).thenReturn(exam);
        when(examRepository.save(captor.capture())).thenReturn(savedExam);
        when(examMapper.toExamResponseDto(savedExam)).thenReturn(responseDto);

        ExamResponseDto result = service.createExam(1L, request);

        Exam saved = captor.getValue();
        assertEquals("Test Exam", saved.getTitle());
        assertEquals("desc", saved.getDescription());
        assertEquals(Privacy.PRIVATE, saved.getPrivacy());
        assertEquals(1L, saved.getCreatedBy());
        assertSame(set, saved.getSet());
        assertSame(responseDto, result);
        verify(examRepository).save(any(Exam.class));
        verify(examPermissionService).addOwner(1L, 1L);
        verify(setRepository).updateLastModifiedDate(eq(1L), any(OffsetDateTime.class));
    }

    @Test
    void deleteExamAsOwner() {
        ExamServiceImpl service = new ExamServiceImpl(
                fileService, aiExamService, questionService, authenticationContext,
                examRepository, examQuestionRepository, flashcardRepository,
                cardItemRepository, setRepository, noteRepository, examMapper,
                examPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository, userRepository
        );

        User user = TestFixtures.user(1L);
        Exam exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Test Exam");
        exam.setCreatedBy(1L);

        when(examRepository.findBySetIdAndId(1L, 1L)).thenReturn(Optional.of(exam));

        service.deleteExam(1L, 1L);

        verify(examRepository).delete(exam);
        verify(setRepository).updateLastModifiedDate(eq(1L), any(OffsetDateTime.class));
    }

    @Test
    void deleteExamDeletesWithoutOwnershipCheck() {
        ExamServiceImpl service = new ExamServiceImpl(
                fileService, aiExamService, questionService, authenticationContext,
                examRepository, examQuestionRepository, flashcardRepository,
                cardItemRepository, setRepository, noteRepository, examMapper,
                examPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository, userRepository
        );

        Exam exam = new Exam();
        exam.setId(2L);
        exam.setTitle("Other Exam");
        exam.setCreatedBy(2L);

        when(examRepository.findBySetIdAndId(1L, 2L)).thenReturn(Optional.of(exam));

        service.deleteExam(1L, 2L);

        verify(examRepository).delete(exam);
    }

    @Test
    void getExamWithRoleAndFavoriteStatus() {
        ExamServiceImpl service = new ExamServiceImpl(
                fileService, aiExamService, questionService, authenticationContext,
                examRepository, examQuestionRepository, flashcardRepository,
                cardItemRepository, setRepository, noteRepository, examMapper,
                examPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository, userRepository
        );

        User user = TestFixtures.user(1L);
        User owner = TestFixtures.user(1L);
        owner.setFirstName("John");
        owner.setLastName("Doe");

        Exam exam = new Exam();
        exam.setId(1L);
        exam.setTitle("Test Exam");
        exam.setCreatedBy(1L);

        ExamResponseDto mapperDto = ExamResponseDto.builder()
                .id(1L)
                .title("Test Exam")
                .privacy(Privacy.PRIVATE)
                .description("desc")
                .duration(60L)
                .numQuestions(3L)
                .creationMethod(CreationMethod.MANUAL)
                .build();

        when(examRepository.findBySetIdAndId(1L, 1L)).thenReturn(Optional.of(exam));
        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(examPermissionService.getUserRoleInExam(1L, 1L)).thenReturn(NoteRole.OWNER);
        when(userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(1L, 1L, ContentType.EXAM))
                .thenReturn(true);
        when(examMapper.toExamResponseDto(exam)).thenReturn(mapperDto);
        when(userRepository.findById(1L)).thenReturn(Optional.of(owner));

        ExamResponseDto result = service.getExam(1L, 1L);

        assertEquals(NoteRole.OWNER, result.userRole());
        assertTrue(result.isFavorited());
        assertEquals(1L, result.ownerId());
        assertNotNull(result.ownerName());
    }

    @Test
    void createExamFromFlashcardCopiesCards() {
        ExamServiceImpl service = new ExamServiceImpl(
                fileService, aiExamService, questionService, authenticationContext,
                examRepository, examQuestionRepository, flashcardRepository,
                cardItemRepository, setRepository, noteRepository, examMapper,
                examPermissionService, topicAssignmentAsyncService,
                userFavoriteResourceRepository, userRepository
        );

        User user = TestFixtures.user(1L);
        Set set = new Set();
        set.setId(1L);
        set.setUser(user);

        Flashcard flashcard = new Flashcard();
        flashcard.setId(1L);
        flashcard.setUser(user);
        flashcard.setSet(set);

        CardItem c1 = new CardItem();
        c1.setId(1L);
        c1.setFrontCard("Q1");
        c1.setBackCard("A1");

        CardItem c2 = new CardItem();
        c2.setId(2L);
        c2.setFrontCard("Q2");
        c2.setBackCard("A2");

        List<CardItem> cards = List.of(c1, c2);

        CreateQuestionRequestDto q1 = CreateQuestionRequestDto.builder()
                .content("Q1 content")
                .type("MULTIPLE_CHOICE")
                .point(10)
                .build();
        CreateQuestionRequestDto q2 = CreateQuestionRequestDto.builder()
                .content("Q2 content")
                .type("MULTIPLE_CHOICE")
                .point(10)
                .build();
        List<CreateQuestionRequestDto> aiQuestions = List.of(q1, q2);

        CreateExamFromReviewRequestDto aiGeneratedContent = CreateExamFromReviewRequestDto.builder()
                .title("Generated Exam")
                .description("AI generated")
                .duration(30L)
                .questions(aiQuestions)
                .build();

        Exam savedExam = new Exam();
        savedExam.setId(2L);
        savedExam.setTitle("Generated Exam");
        savedExam.setSet(set);
        savedExam.setCreatedBy(1L);

        ExamResponseDto responseDto = ExamResponseDto.builder()
                .id(2L)
                .title("Generated Exam")
                .build();

        QuestionListResponseDto questionListResponse = QuestionListResponseDto.builder()
                .questions(List.of())
                .build();

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(flashcardRepository.findByIdAndSetId(1L, 1L)).thenReturn(Optional.of(flashcard));
        when(cardItemRepository.findAllByFlashcardId(1L)).thenReturn(cards);
        when(aiExamService.generateExamFromCards(argThat((List<CardContent> contents) ->
                contents.size() == 2))).thenReturn(aiGeneratedContent);
        when(setRepository.getReferenceById(1L)).thenReturn(set);
        when(examRepository.save(any(Exam.class))).thenReturn(savedExam);
        when(examRepository.findById(2L)).thenReturn(Optional.of(savedExam));
        when(questionService.createQuestion(eq(2L), any()))
                .thenReturn(questionListResponse);
        when(examMapper.toExamResponseDto(savedExam)).thenReturn(responseDto);

        service.createExamFromFlashcard(1L, 1L);

        verify(questionService).createQuestion(eq(2L), eq(aiGeneratedContent.questions()));
        verify(topicAssignmentAsyncService).assignTopicsToExamAsync(2L);
        verify(setRepository).updateLastModifiedDate(eq(1L), any(OffsetDateTime.class));
    }
}
