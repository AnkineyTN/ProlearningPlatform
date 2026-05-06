package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.dto.request.flashcard.*;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.service.permission.impl.FlashcardPermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.model.*;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.file.FileService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.set.SetService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class FlashcardServiceImpl implements FlashcardService {
    private final AuthenticationContext authenticationContext;
    private final FlashcardRepository flashcardRepository;
    private final NoteRepository noteRepository;
    private final UserService userService;
    private final SetService setService;
    private final FlashcardMapper flashcardMapper;
    private final AssetService assetService;
    private final CardItemMapper cardItemMapper;
    private final SetRepository setRepository;

    private final FileService fileService;
    private final AIFlashcardService aiFlashcardService;
    private final FlashcardPermissionService flashcardPermissionService;
    private final com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentAsyncService topicAssignmentAsyncService;

    @Override
    public Page<FlashcardResponseDto> getAllFlashcard(Long setId, String q, Privacy privacy, CreationMethod createMethod, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();

        Page<Flashcard> pagedFlashcard;

        String methodFilter = createMethod != null ? createMethod.name() : null;

        if (q == null || q.isBlank()) {
            if (privacy == null) {
                pagedFlashcard = flashcardRepository.findByUserIdAndSetId(userId, setId, methodFilter, pageable);
            } else {
                pagedFlashcard = flashcardRepository.findByUserIdAndSetIdAndPrivacy(userId, setId, privacy.name(), methodFilter, pageable);
            }
        } else {
            if (privacy == null) {
                pagedFlashcard = flashcardRepository.searchByUserIdAndSetId(userId, setId, q, methodFilter, pageable);
            } else {
                pagedFlashcard = flashcardRepository.searchByUserIdAndSetIdAndPrivacy(userId, setId, q, privacy.name(), methodFilter, pageable);
            }
        }

        return pagedFlashcard.map(flashcardMapper::toFlashcardResponseDto);
    }

    @Override
    public DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();

        Flashcard flashcard = flashcardRepository.findByIdAndSetId(flashcardId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + " not found!"));

        NoteRole userRole = flashcardPermissionService.getUserRoleInFlashcard(flashcardId, userId);

        DetailFlashcardResponseDto dto = flashcardMapper.toDetailFlashcardResponseDto(flashcard);
        dto.setUserRole(userRole);
        
        return dto;
    }

    @Override
    @Transactional
    public FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();
        User userFlashcard = userService.getUserById(userId);

        Set setFlashcard = setService.getSetById(setId);

        if (flashcardRepository.existsBySetIdAndTitle(setId, flashcardCreateRequestDto.getTitle())) {
            throw new ResourceAlreadyExistsException("Flashcard already exists in this set");
        }

        Flashcard flashcard = flashcardMapper.toFlashcard(flashcardCreateRequestDto);

        if (flashcardCreateRequestDto.getCards() != null && !flashcardCreateRequestDto.getCards().isEmpty()) {
            Map<Long, Asset> activatedAssetsMap = activateCardImages(flashcardCreateRequestDto.getCards(), userId);

            List<CardItem> cardItems = buildCardItemList(flashcardCreateRequestDto.getCards(), flashcard, activatedAssetsMap);

            flashcard.setCards(cardItems);
        }

        flashcard.setUser(userFlashcard);
        flashcard.setSet(setFlashcard);

        Flashcard savedFlashcard = flashcardRepository.save(flashcard);

        flashcardPermissionService.addOwner(savedFlashcard.getId(), userId);

        if (flashcard.getCreate_method() == CreationMethod.AI) {
            topicAssignmentAsyncService.assignTopicsToFlashcardAsync(savedFlashcard.getId());
        }

        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        return flashcardMapper.toFlashcardResponseDto(savedFlashcard);
    }

    @Override
    @Transactional
    public FlashcardResponseDto addFlashcardFromReview(List<CardItem> sourceCards, String title, String description, Long setId) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);

        Flashcard flashcard = new Flashcard();
        flashcard.setTitle(title);
        flashcard.setDescription(description);
        flashcard.setUser(user);
        flashcard.setPrivacy(Privacy.PRIVATE);
        flashcard.setSet(setId != null ? setService.getSetById(setId) : null);
        flashcard.setCreate_method(CreationMethod.REVIEW);

        List<CardItem> copiedCards = sourceCards.stream()
                .map(source -> CardItem.builder()
                        .flashcard(flashcard)
                        .frontCard(source.getFrontCard())
                        .backCard(source.getBackCard())
                        .topic(source.getTopic())
                        .build())
                .collect(Collectors.toList());

        flashcard.setCards(copiedCards);

        Flashcard saved = flashcardRepository.save(flashcard);
        if (setId != null) {
            setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        }
        return flashcardMapper.toFlashcardResponseDto(saved);
    }

    private Map<Long, Asset> activateCardImages(List<CardItemCreateRequestDto> cardDtos, Long userId) {
        List<Long> assetIdsToActivate = cardDtos.stream()
                .map(CardItemCreateRequestDto::getImageAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return assetService.findAndActivateAssets(assetIdsToActivate, userId);
    }

    private List<CardItem> buildCardItemList(List<CardItemCreateRequestDto> cardItemCreateRequestDtos,
                                             Flashcard flashcard,
                                             Map<Long, Asset> activatedAssetsMap) {

        return cardItemCreateRequestDtos.stream()
                .map(cardDto -> {
                    CardItem cardItem = cardItemMapper.toCardItem(cardDto);

                    if (cardDto.getImageAssetId() != null) {
                        Asset asset = activatedAssetsMap.get(cardDto.getImageAssetId());
                        cardItem.setImage(asset);
                    }

                    cardItem.setFlashcard(flashcard);
                    return cardItem;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteFlashcard(Long setId, Long flashcardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        Flashcard deletedFlashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + "not found!"));
        int deletedCount = flashcardRepository.deleteByIdAndSetIdAndSetUserId(flashcardId, setId, userId);

        if (deletedCount == 0) {
            throw new BadRequestException("Flashcard not found or access denied.");
        }
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
    }

    @Override
    @Transactional
    public FlashcardResponseDto updateFlashcard(Long setId, Long flashcardId, FlashcardUpdatingRequestDto flashcardUpdatingRequestDto) throws BadRequestException {
        Flashcard flashcard = flashcardRepository.getFlashcardByIdAndSetIdAndSetUserId(
                        flashcardId,
                        setId,
                        authenticationContext.getCurrentUserId()
                )
                .orElseThrow(() -> new BadRequestException("Flashcard not found or access denied."));

        flashcardMapper.updateFlashcardFromDto(flashcardUpdatingRequestDto, flashcard);
        Flashcard updatedFlashcard = flashcardRepository.save(flashcard);

        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        return flashcardMapper.toFlashcardResponseDto(updatedFlashcard);
    }

    @Override
    public DetailFlashcardResponseDto updateFlashcard(Flashcard newFlashcard) {
        Flashcard savedFlashcard = flashcardRepository.save(newFlashcard);

        if (savedFlashcard.getSet() != null) {
            setRepository.updateLastModifiedDate(savedFlashcard.getSet().getId(), OffsetDateTime.now());
        }
        return flashcardMapper.toDetailFlashcardResponseDto(savedFlashcard);
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByNotes(GenerateFlashcardByNoteRequestDto request) {
        List<Long> noteIds = request.getNoteIds();
        List<Note> notes = noteRepository.findAllById(noteIds);

        if (notes.isEmpty()) {
            throw new RuntimeException("No notes found with provided IDs");
        }

        List<String> contents = new ArrayList<>();
        for (Note note : notes) {
            String content = "=== Note: " + note.getTitle() + " ===\n" + note.getContent();
            contents.add(content);
        }

        return aiFlashcardService.generateFlashcardByNotes(
                contents,
                request.getFreeText(),
                request.getLanguage()
        );
    }

    @Override
    public Flashcard getFlashcardById(Long flashcardId) {
        return flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id " + flashcardId + " not found!"));
    }

    @Override
    public List<InviteResultResponse> inviteMembers(Long setId, Long flashcardId, InviteMemberRequest request) {
        Long userId = authenticationContext.getCurrentUserId();
        List<InviteResultResponse> results = flashcardPermissionService.inviteMembers(
            setId, flashcardId, request.getTargets(), request.getRole(), userId);

        return results;
    }

    @Override
    public void acceptInvite(Long flashcardId) {
        flashcardPermissionService.acceptInvite(flashcardId, authenticationContext.getCurrentUserId());
    }

    @Override
    public void declineInvite(Long flashcardId) {
        flashcardPermissionService.declineInvite(flashcardId, authenticationContext.getCurrentUserId());
    }

    @Override
    public void removeMember(Long flashcardId, Long targetUserId) {
        flashcardPermissionService.removeMember(flashcardId, targetUserId, authenticationContext.getCurrentUserId());
    }

    @Override
    public AcceptByTokenResponse acceptByToken(String token) {
        AcceptByTokenResponse response = flashcardPermissionService.acceptByToken(token);

        return response;
    }

    @Override
    public List<PendingInviteResponse> getPendingInvites() {
        Long userId = authenticationContext.getCurrentUserId();
        List<InviteResultResponse.PendingInviteFlashcardResponse> flashcardResponses = 
            flashcardPermissionService.getPendingInvites(userId);
        
        return flashcardResponses.stream()
                .map(fc -> new PendingInviteResponse(
                    fc.getFlashcardId(),
                    fc.getFlashcardTitle(),
                    fc.getRole(),
                    fc.getSetId(),
                    fc.getInvitedAt()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
}
