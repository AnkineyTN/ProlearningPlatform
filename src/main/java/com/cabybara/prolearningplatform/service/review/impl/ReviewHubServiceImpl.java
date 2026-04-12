package com.cabybara.prolearningplatform.service.review.impl;

import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.enums.CreationMethod;
<<<<<<< HEAD
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
=======
>>>>>>> dev
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
<<<<<<< HEAD
import com.cabybara.prolearningplatform.repository.SetRepository;
=======
>>>>>>> dev
import com.cabybara.prolearningplatform.service.review.ReviewHubService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
<<<<<<< HEAD
import org.springframework.security.access.AccessDeniedException;
=======
>>>>>>> dev
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewHubServiceImpl implements ReviewHubService {

    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
<<<<<<< HEAD
    private final SetRepository setRepository;
=======
>>>>>>> dev
    private final FlashcardMapper flashcardMapper;
    private final ExamMapper examMapper;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional(readOnly = true)
<<<<<<< HEAD
    public Page<FlashcardResponseDto> getReviewFlashcards(Long setId, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        validateSetOwnership(setId, userId);
        return flashcardRepository
                .findAllBySetIdAndUserIdAndCreateMethod(setId, userId, CreationMethod.REVIEW, pageable)
=======
    public Page<FlashcardResponseDto> getReviewFlashcards(Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        return flashcardRepository
                .findAllByUserIdAndCreateMethod(userId, CreationMethod.REVIEW_AI, pageable)
>>>>>>> dev
                .map(flashcardMapper::toFlashcardResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
<<<<<<< HEAD
    public Page<ExamResponseDto> getReviewExams(Long setId, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        validateSetOwnership(setId, userId);
        return examRepository
                .findAllBySetIdAndCreatedByAndCreationMethod(setId, userId, CreationMethod.REVIEW, pageable)
                .map(examMapper::toExamResponseDto);
    }

    private void validateSetOwnership(Long setId, Long userId) {
        setRepository.findById(setId)
                .ifPresentOrElse(
                        set -> {
                            if (!set.getUser().getId().equals(userId)) {
                                throw new AccessDeniedException("You do not have access to this set");
                            }
                        },
                        () -> { throw new ResourceNotFoundException("Set not found: " + setId); }
                );
    }
=======
    public Page<ExamResponseDto> getReviewExams(Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        return examRepository
                .findAllByCreatedByAndCreationMethod(userId, CreationMethod.REVIEW_AI, pageable)
                .map(examMapper::toExamResponseDto);
    }
>>>>>>> dev
}
