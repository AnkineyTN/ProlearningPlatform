package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.model.*;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.upload.ImageAssetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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
    private final UserRepository userRepository;
    private final SetRepository setRepository;
    private final FlashcardMapper flashcardMapper;
    private final ImageAssetService imageAssetService;

    @Override
    public Page<FlashcardResponseDto> getAllFlashcard(Long setId, Pageable pageable) {
        Long userId = authenticationContext.getCurrentUserId();

        Page<Flashcard> allPageFlashcards = flashcardRepository.findAllBySetIdAndUserId(setId, userId, pageable);

        return allPageFlashcards.map(flashcardMapper::toFlashcardResponseDto);
    }

    @Override
    public DetailFlashcardResponseDto getDetailFlashcard(Long setId, Long flashcardId) {
        Long userId = authenticationContext.getCurrentUserId();

        Flashcard flashcard = flashcardRepository.findByIdAndSetIdAndUserId(flashcardId, setId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + "not found!"));

        return flashcardMapper.toDetailFlashcardResponseDto(flashcard);
    }

    @Override
    @Transactional
    public FlashcardResponseDto addFlashcardManual(Long setId, FlashcardCreateRequestDto flashcardCreateRequestDto) {
        Long userId = authenticationContext.getCurrentUserId();
        User userFlashcard = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User with id: " + userId + "not found!"));

        Set setFlashcard = setRepository.findById(setId)
                .orElseThrow(() -> new ResourceNotFoundException("Set with id: " + setId + "not found!"));

        if (flashcardRepository.existsBySetIdAndTitle(setId, flashcardCreateRequestDto.getTitle())) {
            throw new ResourceAlreadyExistsException("Flashcard already exists in this set");
        }

        Flashcard flashcard = flashcardMapper.toFlashcard(flashcardCreateRequestDto);

        if (flashcardCreateRequestDto.getCards() != null && !flashcardCreateRequestDto.getCards().isEmpty()) {
            Map<Long, ImageAsset> activatedAssetsMap = activateCardImages(flashcardCreateRequestDto.getCards(), userId);

            List<CardItem> cardItems = buildCardItemList(flashcardCreateRequestDto.getCards(), flashcard, activatedAssetsMap);

            flashcard.setCards(cardItems);
        }

        flashcard.setUser(userFlashcard);
        flashcard.setSet(setFlashcard);

        return flashcardMapper.toFlashcardResponseDto(flashcardRepository.save(flashcard));
    }

    @Override
    @Transactional
    public DetailFlashcardResponseDto addCardToFlashcard(Long setId, Long flashcardId, List<CardItemCreateRequestDto> dtos) {
        Flashcard flashcard = flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id " + flashcardId + " not found!"));

        List<CardItem> cardItems = dtos.stream()
                .map(dto -> {
                    CardItem card = flashcardMapper.toCardItem(dto);

                    card.setFlashcard(flashcard);

                    return card;
                })
                .toList();

        flashcard.getCards().addAll(cardItems);

        return flashcardMapper.toDetailFlashcardResponseDto(flashcardRepository.save(flashcard));
    }

    private Map<Long, ImageAsset> activateCardImages(List<CardItemCreateRequestDto> cardDtos, Long userId) {
        List<Long> assetIdsToActivate = cardDtos.stream()
                .map(CardItemCreateRequestDto::getImageAssetId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());

        return imageAssetService.findAndActivateAssets(assetIdsToActivate, userId);
    }

    private List<CardItem> buildCardItemList(List<CardItemCreateRequestDto> cardItemCreateRequestDtos,
                                             Flashcard flashcard,
                                             Map<Long, ImageAsset> activatedAssetsMap) {

        return cardItemCreateRequestDtos.stream()
                .map(cardDto -> {
                    CardItem cardItem = flashcardMapper.toCardItem(cardDto);

                    if (cardDto.getImageAssetId() != null) {
                        ImageAsset asset = activatedAssetsMap.get(cardDto.getImageAssetId());
                        cardItem.setImage(asset);
                    }

                    cardItem.setFlashcard(flashcard);
                    return cardItem;
                })
                .collect(Collectors.toList());
    }
}
