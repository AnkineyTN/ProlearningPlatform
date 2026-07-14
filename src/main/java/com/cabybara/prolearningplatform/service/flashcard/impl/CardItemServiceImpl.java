package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.permission.impl.FlashcardPermissionService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardItemServiceImpl implements CardItemService {
    private final FlashcardService flashcardService;
    private final CardItemMapper cardItemMapper;
    private final AuthenticationContext authenticationContext;
    private final CardItemRepository cardItemRepository;
    private final AssetService assetService;
    private final FlashcardRepository flashcardRepository;
    private final FlashcardPermissionService flashcardPermissionService;

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public DetailFlashcardResponseDto addCardToFlashcard(Long setId, Long flashcardId, List<CardItemCreateRequestDto> dtos) {
        Long userId = authenticationContext.getCurrentUserId();

        if (!flashcardPermissionService.canEdit(userId, flashcardId)) {
            throw new AccessDeniedException("Access denied: cannot add cards to flashcard " + flashcardId);
        }

        Flashcard flashcard = flashcardService.getFlashcardById(flashcardId);

        List<Long> assetIdsToActivate = dtos.stream()
                .map(CardItemCreateRequestDto::getImageAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Asset> activatedAssetsMap = assetService.findAndActivateAssets(assetIdsToActivate, userId);

        List<CardItem> cardItems = dtos.stream()
                .map(dto -> {
                    CardItem card = cardItemMapper.toCardItem(dto);

                    if (dto.getImageAssetId() != null) {
                        Asset asset = activatedAssetsMap.get(dto.getImageAssetId());
                        card.setImage(asset);
                    }
                    card.setFlashcard(flashcard);

                    return card;
                })
                .toList();

        flashcard.getCards().addAll(cardItems);

        return flashcardService.updateFlashcard(flashcard);
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public CardItemResponseDto updateCardItem(Long setId, Long flashcardId, Long cardId, CardItemUpdatingRequestDto updateFlashcardRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();

        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card Item with id: " + cardId + " not found."));

        if (!cardItem.getFlashcard().getId().equals(flashcardId)
                || !cardItem.getFlashcard().getSet().getId().equals(setId)
                || !cardItem.getFlashcard().getSet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied: card does not belong to current user");
        }

        cardItemMapper.updateCardFromDto(updateFlashcardRequestDto, cardItem);

        if (updateFlashcardRequestDto.getImageAssetId() != null) {
            if (cardItem.getImage() != null) {
                assetService.markDeletedAsset(cardItem.getImage());
            }

            Long newImageAssetId = updateFlashcardRequestDto.getImageAssetId();
            Asset newAsset = assetService.findAndActivateAsset(newImageAssetId, userId);

            cardItem.setImage(newAsset);
        }

        CardItemResponseDto result = cardItemMapper.toCardItemResponseDto(cardItemRepository.save(cardItem));
        flashcardRepository.updateUpdatedAt(flashcardId, OffsetDateTime.now());
        return result;
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public List<CardItemResponseDto> updateCardItems(Long setId, Long flashcardId, List<CardItemUpdatingRequestDto> updatingRequestDtos) {
        Long userId = authenticationContext.getCurrentUserId();
        Map<Long, CardItemUpdatingRequestDto> updateDtoMap = prepareUpdateDtoMap(updatingRequestDtos);

        if (updateDtoMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<CardItem> cardItemsToUpdate = fetchAndValidateExistence(updateDtoMap);

        applyUpdatesAndValidateOwnership(setId, flashcardId, cardItemsToUpdate, updateDtoMap, userId);

        cardItemRepository.saveAll(cardItemsToUpdate);
        flashcardRepository.updateUpdatedAt(flashcardId, OffsetDateTime.now());

        return cardItemsToUpdate.stream()
                .map(cardItemMapper::toCardItemResponseDto)
                .toList();
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public void deleteCard(Long setId, Long flashcardId, Long cardId) {
        Long userId = authenticationContext.getCurrentUserId();
        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found."));

        if (!cardItem.getFlashcard().getId().equals(flashcardId) ||
                !cardItem.getFlashcard().getSet().getId().equals(setId) ||
                !cardItem.getFlashcard().getSet().getUser().getId().equals(userId)) {

            throw new AccessDeniedException("The card does not match the provided ownership criteria.");
        }

        if (cardItem.getImage() != null) {
            assetService.markDeletedAsset(cardItem.getImage());
        }

        cardItemRepository.delete(cardItem);
        flashcardRepository.updateUpdatedAt(flashcardId, OffsetDateTime.now());
    }

    @Override
    public List<CardItem> getCardsForReview(Long setId, Long flashcardId, int limit) {
        Long userId = authenticationContext.getCurrentUserId();
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id " + flashcardId + " not found"));

        if (!Objects.equals(flashcard.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to review this flashcard.");
        }

        if (!Objects.equals(flashcard.getSet().getId(), setId)) {
            throw new ResourceNotFoundException("Flashcard does not belong to this set.");
        }

        List<CardItem> dueCards = cardItemRepository.findDueCards(flashcardId, OffsetDateTime.now(), PageRequest.of(0, limit));

        List<CardItem> finalQueue = new ArrayList<>(dueCards);

        // not enough limit param
        if (finalQueue.size() < limit) {
            int remain = limit - finalQueue.size();
            List<CardItem> newCards = cardItemRepository.findNewCards(flashcardId, PageRequest.of(0, remain));
            finalQueue.addAll(newCards);
        }

        return finalQueue.stream().distinct().toList();
    }

    @Override
    public List<CardItem> getAllCardsForReview(Long setId, Long flashcardId, int limit) {
        Long userId = authenticationContext.getCurrentUserId();
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id " + flashcardId + " not found"));

        if (!Objects.equals(flashcard.getUser().getId(), userId)) {
            throw new AccessDeniedException("You are not allowed to review this flashcard.");
        }

        if (!Objects.equals(flashcard.getSet().getId(), setId)) {
            throw new ResourceNotFoundException("Flashcard does not belong to this set.");
        }

        // Return all cards in random order for review mode
        return cardItemRepository.findAllByFlashcardIdRandomOrder(flashcardId, PageRequest.of(0, limit));
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public void deleteCards(Long setId, Long flashcardId, List<Long> cardIds) {
        if (cardIds.isEmpty()) {
            return;
        }

        Long userId = authenticationContext.getCurrentUserId();
        List<CardItem> cards = cardItemRepository.findAllWithImageByIdIn(cardIds);

        if (cards.isEmpty()) {
            throw new ResourceNotFoundException("Cards not found with given IDs.");
        }

        for (CardItem card : cards) {
            if (!card.getFlashcard().getId().equals(flashcardId) ||
                    !card.getFlashcard().getSet().getId().equals(setId) ||
                    !card.getFlashcard().getSet().getUser().getId().equals(userId)) {
                throw new AccessDeniedException("The card does not match the provided ownership criteria.");
            }
        }

        cards.stream()
                .map(CardItem::getImage)
                .filter(Objects::nonNull)
                .forEach(assetService::markDeletedAsset);

        cardItemRepository.deleteAll(cards);
        flashcardRepository.updateUpdatedAt(flashcardId, OffsetDateTime.now());
    }

    private Map<Long, CardItemUpdatingRequestDto> prepareUpdateDtoMap(
            List<CardItemUpdatingRequestDto> updatingRequestDtos
    ) {
        return updatingRequestDtos.stream()
                .filter(dto -> dto.getId() != null)
                .collect(Collectors.toMap(
                        CardItemUpdatingRequestDto::getId,
                        dto -> dto,
                                                (existing, replacement) -> existing // Handle duplicate IDs
                ));
    }

    private List<CardItem> fetchAndValidateExistence(
            Map<Long, CardItemUpdatingRequestDto> updateDtoMap
    ) throws ResourceNotFoundException {

        List<Long> cardItemToUpdateIds = new ArrayList<>(updateDtoMap.keySet());
        List<CardItem> cardItemsToUpdate = cardItemRepository.findAllByIdIn(cardItemToUpdateIds);

        if (cardItemsToUpdate.size() != cardItemToUpdateIds.size()) {
            Set<Long> foundIds = cardItemsToUpdate.stream().map(CardItem::getId).collect(Collectors.toSet());
            List<Long> missingIds = cardItemToUpdateIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .toList();

            throw new ResourceNotFoundException("Some card items not found with IDs: " + missingIds);
        }

        return cardItemsToUpdate;
    }

    private void applyUpdatesAndValidateOwnership(
            Long setId,
            Long flashcardId,
            List<CardItem> cardItemsToUpdate,
            Map<Long, CardItemUpdatingRequestDto> updateDtoMap,
            Long userId
    ) {
        for (CardItem cardItem : cardItemsToUpdate) {
            if (!cardItem.getFlashcard().getSet().getId().equals(setId)) {
                throw new AccessDeniedException("Card Item with ID " + cardItem.getId() + " does not belong to Set ID " + setId);
            }

            if (!cardItem.getFlashcard().getId().equals(flashcardId)) {
                throw new AccessDeniedException("Card Item with ID " + cardItem.getId() + " is linked to the wrong Flashcard.");
            }

            if (!cardItem.getFlashcard().getUser().getId().equals(userId)) {
                throw new AccessDeniedException("Access Denied: Card Item with ID " + cardItem.getId() +
                        " does not belong to the current User ID " + userId);
            }

            CardItemUpdatingRequestDto dto = updateDtoMap.get(cardItem.getId());
            cardItemMapper.updateCardFromDto(dto, cardItem);

            updateCardImage(cardItem, dto, userId);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "flashcard_detail", allEntries = true)
    public CardItemResponseDto deleteCardImage(Long setId, Long flashcardId, Long cardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found."));

        if (!cardItem.getFlashcard().getId().equals(flashcardId) ||
                !cardItem.getFlashcard().getSet().getId().equals(setId) ||
                !cardItem.getFlashcard().getSet().getUser().getId().equals(userId)) {
            throw new AccessDeniedException("The card does not match the provided ownership criteria.");
        }

        if (cardItem.getImage() == null) {
            throw new BadRequestException("This card does not have an image.");
        }

        assetService.markDeletedAsset(cardItem.getImage());
        cardItem.setImage(null);

        CardItemResponseDto result = cardItemMapper.toCardItemResponseDto(cardItemRepository.save(cardItem));
        flashcardRepository.updateUpdatedAt(flashcardId, OffsetDateTime.now());
        return result;
    }

    private void updateCardImage(CardItem cardItem, CardItemUpdatingRequestDto dto, Long userId) {
        Long newImageAssetId = dto.getImageAssetId();

        if (newImageAssetId == null) {
            return;
        }

        if (cardItem.getImage() != null) {
            assetService.markDeletedAsset(cardItem.getImage());
        }

        Asset newAsset = assetService.findAndActivateAsset(newImageAssetId, userId);

        cardItem.setImage(newAsset);
    }
}
