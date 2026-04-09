package com.cabybara.prolearningplatform.service.review.impl;

import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.mapper.ExamMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.service.review.ReviewHubService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewHubServiceImpl implements ReviewHubService {

    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;
    private final FlashcardMapper flashcardMapper;
    private final ExamMapper examMapper;
    private final AuthenticationContext authenticationContext;

    @Override
    @Transactional(readOnly = true)
    public Page<FlashcardResponseDto> getReviewFlashcards(Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        return flashcardRepository
                .findAllByUserIdAndCreateMethod(userId, CreationMethod.REVIEW_AI, pageable)
                .map(flashcardMapper::toFlashcardResponseDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ExamResponseDto> getReviewExams(Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        return examRepository
                .findAllByCreatedByAndCreationMethod(userId, CreationMethod.REVIEW_AI, pageable)
                .map(examMapper::toExamResponseDto);
    }
}
