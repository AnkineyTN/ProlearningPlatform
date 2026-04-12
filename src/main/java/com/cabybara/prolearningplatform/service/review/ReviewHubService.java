package com.cabybara.prolearningplatform.service.review;

import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ReviewHubService {

<<<<<<< HEAD
    Page<FlashcardResponseDto> getReviewFlashcards(Long setId, Pageable pageable);

    Page<ExamResponseDto> getReviewExams(Long setId, Pageable pageable);
=======
    Page<FlashcardResponseDto> getReviewFlashcards(Pageable pageable);

    Page<ExamResponseDto> getReviewExams(Pageable pageable);
>>>>>>> dev
}
