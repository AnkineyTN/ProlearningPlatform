package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.model.Flashcard;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.service.flashcard.CardItemService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.springframework.stereotype.Service;

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

    @Override
    @Transactional
    public DetailFlashcardResponseDto addCardToFlashcard(Long setId, Long flashcardId, List<CardItemCreateRequestDto> dtos) {
        Long userId = authenticationContext.getCurrentUserId();

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
    public CardItemResponseDto updateCardItem(Long setId, Long flashcardId, Long cardId, CardItemUpdatingRequestDto updateFlashcardRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();
        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card Item with id: " + cardId + " not found."));

        cardItemMapper.updateCardFromDto(updateFlashcardRequestDto, cardItem);

        if (updateFlashcardRequestDto.getImageAssetId() != null) {
            if (cardItem.getImage() != null) {
                assetService.markDeletedAsset(cardItem.getImage());
            }

            Long newImageAssetId = updateFlashcardRequestDto.getImageAssetId();
            Asset newAsset = assetService.findAndActivateAsset(newImageAssetId, userId);

            cardItem.setImage(newAsset);
        }

        return cardItemMapper.toCardItemResponseDto(cardItemRepository.save(cardItem));
    }

    @Override
    @Transactional
    public List<CardItemResponseDto> updateCardItems(Long setId, Long flashcardId, List<CardItemUpdatingRequestDto> updatingRequestDtos) {
        Long userId = authenticationContext.getCurrentUserId();
        Map<Long, CardItemUpdatingRequestDto> updateDtoMap = prepareUpdateDtoMap(updatingRequestDtos);

        if (updateDtoMap.isEmpty()) {
            return Collections.emptyList();
        }

        List<CardItem> cardItemsToUpdate = fetchAndValidateExistence(updateDtoMap);

        applyUpdatesAndValidateOwnership(setId, flashcardId, cardItemsToUpdate, updateDtoMap, userId);

        cardItemRepository.saveAll(cardItemsToUpdate);

        return cardItemsToUpdate.stream()
                .map(cardItemMapper::toCardItemResponseDto)
                .toList();
    }

    @Override
    @Transactional
    public void deleteCard(Long setId, Long flashcardId, Long cardId) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();
        CardItem cardItem = cardItemRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found."));

        if (!cardItem.getFlashcard().getId().equals(flashcardId) ||
                !cardItem.getFlashcard().getSet().getId().equals(setId) ||
                !cardItem.getFlashcard().getSet().getUser().getId().equals(userId)) {

            throw new BadRequestException("The card does not match the provided ownership criteria.");
        }

        cardItemRepository.delete(cardItem);
    }

    @Override
    @Transactional
    public void deleteCards(Long setId, Long flashcardId, List<Long> cardIds) throws BadRequestException {
        Long userId = authenticationContext.getCurrentUserId();

        int deletedCount = cardItemRepository.deleteAllByIdInAndOwnershipChecks(
                cardIds,
                flashcardId,
                setId,
                userId
        );

        if (deletedCount == 0 && !cardIds.isEmpty()) {
            throw new BadRequestException("Flashcard not found or access denied.");
        }
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
    ) throws SecurityException {
        for (CardItem cardItem : cardItemsToUpdate) {
            if (!cardItem.getFlashcard().getSet().getId().equals(setId)) {
                throw new SecurityException("Card Item with ID " + cardItem.getId() + " does not belong to Set ID " + setId);
            }

            if (!cardItem.getFlashcard().getId().equals(flashcardId)) {
                throw new SecurityException("Card Item with ID " + cardItem.getId() + " is linked to the wrong Flashcard.");
            }

            if (!cardItem.getFlashcard().getUser().getId().equals(userId)) {
                throw new SecurityException("Access Denied: Card Item with ID " + cardItem.getId() +
                        " does not belong to the current User ID " + userId);
            }

            CardItemUpdatingRequestDto dto = updateDtoMap.get(cardItem.getId());
            cardItemMapper.updateCardFromDto(dto, cardItem);

            updateCardImage(cardItem, dto, userId);
        }
    }

    private void updateCardImage(CardItem cardItem, CardItemUpdatingRequestDto dto, Long userId) {
        Long newImageAssetId = dto.getImageAssetId();

        if (newImageAssetId == null) {
            return;
        }

        if (cardItem.getImage().getUrl() != null) {
            assetService.markDeletedAsset(cardItem.getImage());
        }

        Asset newAsset = assetService.findAndActivateAsset(newImageAssetId, userId);

        cardItem.setImage(newAsset);
    }
}
