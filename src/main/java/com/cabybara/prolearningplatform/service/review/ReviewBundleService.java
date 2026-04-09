package com.cabybara.prolearningplatform.service.review;

import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;

import java.util.List;

public interface ReviewBundleService {

    ReviewBundle createBundle(Long userId, List<Long> cardIds,
                              java.time.OffsetDateTime periodFrom,
                              java.time.OffsetDateTime periodTo,
                              java.time.OffsetDateTime expiresAt);

    ReviewBundleResponseDto getBundle(Long bundleId);

    GenerateFlashcardByAIResponseDto generateFlashcard(Long bundleId);

    GenerateExamByAIResponseDto generateExam(Long bundleId);
}
