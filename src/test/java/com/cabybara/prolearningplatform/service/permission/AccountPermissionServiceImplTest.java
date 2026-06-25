package com.cabybara.prolearningplatform.service.permission;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.permission.impl.AccountPermissionServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountPermissionServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NoteRepository noteRepository;
    @Mock
    private FlashcardRepository flashcardRepository;
    @Mock
    private ExamRepository examRepository;

    @Test
    void accountTypeChecksDefaultToFreeWhenMissing() {
        AccountPermissionServiceImpl service = new AccountPermissionServiceImpl(userRepository, noteRepository, flashcardRepository, examRepository);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertFalse(service.isPro(1L));
        assertTrue(service.isFree(1L));
    }

    @Test
    void quotaChecksAlwaysAllowProUsers() {
        AccountPermissionServiceImpl service = new AccountPermissionServiceImpl(userRepository, noteRepository, flashcardRepository, examRepository);
        User user = TestFixtures.user(2L);
        user.setAccountType(AccountType.PRO);
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        assertTrue(service.canCreateNote(2L));
        assertTrue(service.canCreateFlashcard(2L));
        assertTrue(service.canCreateExam(2L));
        verify(noteRepository, never()).countNumOfNoteByCreatedUser(2L);
        verify(flashcardRepository, never()).countNumOfFlashcardByCreatedUser(2L);
        verify(examRepository, never()).countNumOfExamByCreatedBy(2L);
    }

    @Test
    void quotaChecksUseConfiguredLimitsForFreeUsers() {
        AccountPermissionServiceImpl service = new AccountPermissionServiceImpl(userRepository, noteRepository, flashcardRepository, examRepository);
        ReflectionTestUtils.setField(service, "freeMaxNotes", 2);
        ReflectionTestUtils.setField(service, "freeMaxFlashcards", 3);
        ReflectionTestUtils.setField(service, "freeMaxExams", 1);
        User user = TestFixtures.user(3L);
        user.setAccountType(AccountType.FREE);

        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(noteRepository.countNumOfNoteByCreatedUser(3L)).thenReturn(2L);
        when(flashcardRepository.countNumOfFlashcardByCreatedUser(3L)).thenReturn(1L);
        when(examRepository.countNumOfExamByCreatedBy(3L)).thenReturn(1L);

        assertFalse(service.canCreateNote(3L));
        assertTrue(service.canCreateFlashcard(3L));
        assertFalse(service.canCreateExam(3L));
    }

    @Test
    void canAccessFeatureAllowsUnknownAndRequiresProForKnownFeatures() {
        AccountPermissionServiceImpl service = new AccountPermissionServiceImpl(userRepository, noteRepository, flashcardRepository, examRepository);
        User freeUser = TestFixtures.user(4L);
        freeUser.setAccountType(AccountType.FREE);
        User proUser = TestFixtures.user(5L);
        proUser.setAccountType(AccountType.PRO);

        when(userRepository.findById(4L)).thenReturn(Optional.of(freeUser));
        when(userRepository.findById(5L)).thenReturn(Optional.of(proUser));

        assertTrue(service.canAccessFeature(4L, "UNKNOWN_FEATURE"));
        assertFalse(service.canAccessFeature(4L, FeatureKey.AI_GENERATION));
        assertTrue(service.canAccessFeature(5L, FeatureKey.AI_GENERATION));
    }

    @Test
    void nullAccountTypeFallsBackToFree() {
        AccountPermissionServiceImpl service = new AccountPermissionServiceImpl(userRepository, noteRepository, flashcardRepository, examRepository);
        User user = TestFixtures.user(6L);
        user.setAccountType(null);
        when(userRepository.findById(6L)).thenReturn(Optional.of(user));

        assertTrue(service.isFree(6L));
        assertFalse(service.isPro(6L));
    }
}
