package com.cabybara.prolearningplatform.service.review;

import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReviewBundleService {

    ReviewBundle createBundle(Long userId, List<Long> cardIds,
                              OffsetDateTime periodFrom,
                              OffsetDateTime periodTo,
                              OffsetDateTime expiresAt);

    ReviewBundleResponseDto getBundle(Long bundleId);

    FlashcardResponseDto generateFlashcard(Long bundleId);

    ExamResponseDto generateExam(Long bundleId);
}
