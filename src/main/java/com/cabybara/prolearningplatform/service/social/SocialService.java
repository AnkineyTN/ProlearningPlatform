package com.cabybara.prolearningplatform.service.social;

import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SocialService {
    Page<SocialItemResponseDto> getSocialNotes(String q, Pageable pageable);
    Page<SocialItemResponseDto> getSocialFlashcards(String q, Pageable pageable);
    Page<SocialItemResponseDto> getSocialExams(String q, Pageable pageable);
}
