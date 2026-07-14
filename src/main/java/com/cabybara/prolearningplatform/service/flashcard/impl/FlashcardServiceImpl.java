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
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.SharedFlashcardResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.model.*;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.ai.AIFlashcardService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.set.SetService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.user.UserService;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
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

    private final AIFlashcardService aiFlashcardService;
    private final FlashcardPermissionService flashcardPermissionService;
    private final NotePermissionService notePermissionService;
    private final com.cabybara.prolearningplatform.service.knowledge.TopicAssignmentAsyncService topicAssignmentAsyncService;
    private final com.cabybara.prolearningplatform.repository.UserFavoriteResourceRepository userFavoriteResourceRepository;

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

        List<Long> flashcardIds = pagedFlashcard.map(Flashcard::getId).getContent();
        java.util.Set<Long> favoritedIds = flashcardIds.isEmpty() ? Collections.emptySet() :
                userFavoriteResourceRepository.findFavoritedResourceIds(userId, flashcardIds, com.cabybara.prolearningplatform.enums.ContentType.FLASHCARD);

        return pagedFlashcard.map(flashcard -> {
            FlashcardResponseDto dto = flashcardMapper.toFlashcardResponseDto(flashcard);
            dto.setIsFavorited(favoritedIds.contains(flashcard.getId()));
            dto.setOwnerId(flashcard.getUser().getId());
            dto.setOwnerName(flashcard.getUser().getFirstName() != null ? flashcard.getUser().getFirstName() + " " + flashcard.getUser().getLastName() : flashcard.getUser().getLastName());
            dto.setOwnerAvatar(flashcard.getUser().getAvatarUrl());
            return dto;
        });
    }

    @Override
    @Cacheable(value = "flashcard_detail", key = "@authenticationContext.getCurrentUserId() + ':' + #setId + ':' + #flashcardId")
    public DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();

        Flashcard flashcard = flashcardRepository.findByIdAndSetId(flashcardId, setId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + " not found!"));

        NoteRole userRole = flashcardPermissionService.getUserRoleInFlashcard(flashcardId, userId);
        boolean isFavorited = userFavoriteResourceRepository.existsByUserIdAndResourceIdAndResourceType(userId, flashcardId, com.cabybara.prolearningplatform.enums.ContentType.FLASHCARD);

        DetailFlashcardResponseDto dto = flashcardMapper.toDetailFlashcardResponseDto(flashcard);
        dto.setUserRole(userRole);
        dto.setIsFavorited(isFavorited);
        dto.setOwnerId(flashcard.getUser().getId());
        dto.setOwnerName(flashcard.getUser().getFirstName() != null ? flashcard.getUser().getFirstName() + " " + flashcard.getUser().getLastName() : flashcard.getUser().getLastName());
        dto.setOwnerAvatar(flashcard.getUser().getAvatarUrl());
        
        return dto;
    }

    @Override
    @Transactional
    public FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();
        User userFlashcard = userService.getUserById(userId);

        Set setFlashcard = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id " + setId + " not found"));

        if (!Objects.equals(setFlashcard.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to add flashcards to this set.");
        }

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
            topicAssignmentAsyncService.assignTopicsToFlashcardAsync(savedFlashcard.getId(), userId);
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
                .toList();

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
                .toList();

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
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public void deleteFlashcard(Long setId, Long flashcardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        Flashcard deletedFlashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + " not found!"));

        if (!Objects.equals(deletedFlashcard.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to delete this flashcard.");
        }

        if (!Objects.equals(deletedFlashcard.getSet().getId(), setId)) {
            throw new BadRequestException("Flashcard does not belong to this set.");
        }

        flashcardRepository.delete(deletedFlashcard);
        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public FlashcardResponseDto updateFlashcard(Long setId, Long flashcardId, FlashcardUpdatingRequestDto flashcardUpdatingRequestDto) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + " not found!"));

        if (!Objects.equals(flashcard.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to update this flashcard.");
        }

        if (!Objects.equals(flashcard.getSet().getId(), setId)) {
            throw new BadRequestException("Flashcard does not belong to this set.");
        }

        flashcardMapper.updateFlashcardFromDto(flashcardUpdatingRequestDto, flashcard);
        Flashcard updatedFlashcard = flashcardRepository.save(flashcard);

        setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());
        return flashcardMapper.toFlashcardResponseDto(updatedFlashcard);
    }

    @Override
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public DetailFlashcardResponseDto updateFlashcard(Flashcard newFlashcard) {
        Flashcard savedFlashcard = flashcardRepository.save(newFlashcard);

        if (savedFlashcard.getSet() != null) {
            setRepository.updateLastModifiedDate(savedFlashcard.getSet().getId(), OffsetDateTime.now());
        }
        return flashcardMapper.toDetailFlashcardResponseDto(savedFlashcard);
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByNotes(GenerateFlashcardByNoteRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        validateNoteAccess(request.getNotes(), userId);
        return generateFlashcardByNotesInternal(request);
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByNotesForTargetSet(GenerateFlashcardByNoteToSetRequestDto request) {
        Long userId = authenticationContext.getCurrentUserId();
        validateTargetSetOwnership(request.getTargetSetId(), userId);
        validateNoteAccess(request.getNotes(), userId);

        GenerateFlashcardByNoteRequestDto generateRequest = GenerateFlashcardByNoteRequestDto.builder()
                .notes(request.getNotes())
                .freeText(request.getFreeText())
                .language(request.getLanguage())
                .build();

        return generateFlashcardByNotesInternal(generateRequest);
    }

    private GenerateFlashcardByAIResponseDto generateFlashcardByNotesInternal(GenerateFlashcardByNoteRequestDto request) {
        // Get note IDs from the request
        List<Long> noteIds = request.getNotes().stream()
                .map(NoteRequestDto::getNoteId)
                .toList();
        
        List<Note> notes = noteRepository.findAllById(noteIds);

        if (notes.isEmpty()) {
            throw new RuntimeException("No notes found with provided IDs");
        }

        // Build a map of noteId to document URLs for quick lookup
        Map<Long, List<String>> noteIdToUrlsMap = request.getNotes().stream()
                .collect(Collectors.toMap(
                        NoteRequestDto::getNoteId,
                        NoteRequestDto::getDocumentUrls
                ));

        // Build NoteContentDto list with content and URLs
        List<NoteContentDto> noteContents = new ArrayList<>();
        for (Note note : notes) {
            List<String> documentUrls = noteIdToUrlsMap.getOrDefault(note.getId(), new ArrayList<>());
            
            NoteContentDto noteContent = NoteContentDto.builder()
                    .noteId(note.getId())
                    .content(note.getContent())
                    .documentUrls(documentUrls)
                    .build();

            noteContents.add(noteContent);
        }

        // Build the AI request DTO
        AIGenerateFlashcardByNoteRequestDto aiRequest = AIGenerateFlashcardByNoteRequestDto.builder()
                .notes(noteContents)
                .freeText(request.getFreeText())
                .language(request.getLanguage())
                .build();

        return aiFlashcardService.generateFlashcardByNotes(aiRequest);
    }

    private void validateTargetSetOwnership(Long targetSetId, Long userId) {
        Set targetSet = setRepository.findById(targetSetId)
                .orElseThrow(() -> new ResourceNotFoundException(targetSetId.toString()));

        if (!Objects.equals(targetSet.getUser().getId(), userId)) {
            throw new AccessDeniedException(userId.toString());
        }
    }

    private void validateNoteAccess(List<NoteRequestDto> notes, Long userId) {
        for (NoteRequestDto noteRequest : notes) {
            if (!notePermissionService.hasAccess(userId, noteRequest.getNoteId())) {
                throw new AccessDeniedException(noteRequest.getNoteId().toString());
            }
        }
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
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public void acceptInvite(Long flashcardId) {
        flashcardPermissionService.acceptInvite(flashcardId, authenticationContext.getCurrentUserId());
    }

    @Override
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public void declineInvite(Long flashcardId) {
        flashcardPermissionService.declineInvite(flashcardId, authenticationContext.getCurrentUserId());
    }

    @Override
    @CacheEvict(value = "flashcard_detail", allEntries = true)
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
                .toList();
    }

    @Override
    public Page<SharedFlashcardResponseDto> getSharedFlashcards(String q, Privacy privacy, CreationMethod createMethod, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();
        String privacyFilter = privacy != null ? privacy.name() : null;
        String methodFilter = createMethod != null ? createMethod.name() : null;

        Page<Flashcard> pagedFlashcards = flashcardRepository.findSharedFlashcards(userId, q, privacyFilter, methodFilter, pageable);

        List<Long> flashcardIds = pagedFlashcards.map(Flashcard::getId).getContent();
        java.util.Set<Long> favoritedIds = flashcardIds.isEmpty() ? Collections.emptySet() :
                userFavoriteResourceRepository.findFavoritedResourceIds(userId, flashcardIds, com.cabybara.prolearningplatform.enums.ContentType.FLASHCARD);

        return pagedFlashcards
                .map(flashcard -> {
                    NoteRole role = flashcardPermissionService.getUserRoleInFlashcard(flashcard.getId(), userId);
                    FlashcardResponseDto dto = flashcardMapper.toFlashcardResponseDto(flashcard);
                    
                    return SharedFlashcardResponseDto.builder()
                            .id(dto.getId())
                            .title(dto.getTitle())
                            .description(dto.getDescription())
                            .status(dto.getStatus())
                            .privacy(dto.getPrivacy())
                            .lastStudy(dto.getLastStudy())
                            .known(dto.getKnown())
                            .learning(dto.getLearning())
                            .remain(dto.getRemain())
                            .createMethod(dto.getCreateMethod())
                            .numCards(dto.getNumCards())
                            .createdAt(dto.getCreatedAt())
                            .updatedAt(dto.getUpdatedAt())
                            .userRole(role)
                            .setId(flashcard.getSet() != null ? flashcard.getSet().getId() : null)
                            .isFavorited(favoritedIds.contains(flashcard.getId()))
                            .ownerId(flashcard.getUser().getId())
                            .ownerName(flashcard.getUser().getFirstName() != null ? flashcard.getUser().getFirstName() + " " + flashcard.getUser().getLastName() : flashcard.getUser().getLastName())
                            .ownerAvatar(flashcard.getUser().getAvatarUrl())
                            .build();
                });
    }
}

