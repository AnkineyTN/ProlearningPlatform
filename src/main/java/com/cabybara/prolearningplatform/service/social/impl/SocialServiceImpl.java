package com.cabybara.prolearningplatform.service.social.impl;

import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.service.social.SocialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class SocialServiceImpl implements SocialService {

    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;

    @Override
    public Page<SocialItemResponseDto> getSocialNotes(String q, Pageable pageable) {
        return noteRepository.findSocialNotes(q, pageable)
                .map(sn -> new SocialItemResponseDto(
                        sn.getId(),
                        "NOTE",
                        sn.getTitle(),
                        sn.getDescription(),
                        sn.getCreatedAt(),
                        sn.getUpdatedAt(),
                        sn.getOwnerId(),
                        buildOwnerName(sn.getOwnerFirstName(), sn.getOwnerLastName()),
                        null,
                        null,
                        null
                ));
    }

    @Override
    public Page<SocialItemResponseDto> getSocialFlashcards(String q, Pageable pageable) {
        return flashcardRepository.findSocialFlashcards(q, pageable)
                .map(sf -> new SocialItemResponseDto(
                        sf.getId(),
                        "FLASHCARD",
                        sf.getTitle(),
                        sf.getDescription(),
                        sf.getCreatedAt(),
                        sf.getUpdatedAt(),
                        sf.getOwnerId(),
                        buildOwnerName(sf.getOwnerFirstName(), sf.getOwnerLastName()),
                        sf.getNumCards(),
                        null,
                        null
                ));
    }

    @Override
    public Page<SocialItemResponseDto> getSocialExams(String q, Pageable pageable) {
        return examRepository.findSocialExams(q, pageable)
                .map(se -> new SocialItemResponseDto(
                        se.getId(),
                        "EXAM",
                        se.getTitle(),
                        se.getDescription(),
                        se.getCreatedAt(),
                        se.getUpdatedAt(),
                        se.getOwnerId(),
                        buildOwnerName(se.getOwnerFirstName(), se.getOwnerLastName()),
                        null,
                        se.getNumQuestions(),
                        se.getDuration()
                ));
    }

    private String buildOwnerName(String firstName, String lastName) {
        return String.format("%s %s",
                Objects.requireNonNullElse(firstName, ""),
                Objects.requireNonNullElse(lastName, "")
        ).trim();
    }
}
