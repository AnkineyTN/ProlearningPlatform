package com.cabybara.prolearningplatform.service.review;

import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
<<<<<<< HEAD
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleListItemDto;
=======
>>>>>>> dev
import com.cabybara.prolearningplatform.dto.response.review.ReviewBundleResponseDto;
import com.cabybara.prolearningplatform.model.review.ReviewBundle;

import java.time.OffsetDateTime;
import java.util.List;

public interface ReviewBundleService {

<<<<<<< HEAD
    ReviewBundle createBundle(Long userId, Long setId, List<Long> cardIds,
                              OffsetDateTime periodFrom,
                              OffsetDateTime periodTo);

    List<ReviewBundleListItemDto> getBundles();

    ReviewBundleResponseDto getBundle(Long bundleId);

    void dismissBundle(Long bundleId);

=======
    ReviewBundle createBundle(Long userId, List<Long> cardIds,
                              OffsetDateTime periodFrom,
                              OffsetDateTime periodTo,
                              OffsetDateTime expiresAt);

    ReviewBundleResponseDto getBundle(Long bundleId);

>>>>>>> dev
    FlashcardResponseDto generateFlashcard(Long bundleId);

    ExamResponseDto generateExam(Long bundleId);
}
