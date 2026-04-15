package com.cabybara.prolearningplatform.service.flashcard;

import com.cabybara.prolearningplatform.dto.request.flashcard.*;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import org.apache.coyote.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface FlashcardService {
    Page<FlashcardResponseDto> getAllFlashcard(Long setId, String q, Privacy privacy, CreationMethod createMethod, Pageable pageable);

    DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId);

    FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto);

    FlashcardResponseDto addFlashcardFromReview(List<CardItem> sourceCards, String title, String description, Long setId);

    void deleteFlashcard(Long setId, Long flashcardId) throws BadRequestException;

    Flashcard getFlashcardById(Long flashcardId);

    FlashcardResponseDto updateFlashcard(Long setId, Long flashcardId, FlashcardUpdatingRequestDto flashcardUpdatingRequestDto) throws BadRequestException;

    DetailFlashcardResponseDto updateFlashcard(Flashcard newFlashcard);

    GenerateFlashcardByAIResponseDto generateFlashcardByNotes(GenerateFlashcardByNoteRequestDto request);

    List<InviteResultResponse> inviteMembers(Long setId, Long flashcardId, InviteMemberRequest request);

    void acceptInvite(Long flashcardId);

    void declineInvite(Long flashcardId);

    void removeMember(Long flashcardId, Long targetUserId);

    AcceptByTokenResponse acceptByToken(String token);
}
