package com.cabybara.prolearningplatform.service.flashcard.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByFileRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.GenerateFlashcardByNoteRequestDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceAlreadyExistsException;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.CardItemMapper;
import com.cabybara.prolearningplatform.mapper.FlashcardMapper;
import com.cabybara.prolearningplatform.model.*;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    private final FileService fileService;
    private final AIFlashcardService aiFlashcardService;

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
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id: " + flashcardId + " not found!"));

        return flashcardMapper.toDetailFlashcardResponseDto(flashcard);
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

        return flashcardMapper.toFlashcardResponseDto(flashcardRepository.save(flashcard));
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
        int deletedCount = flashcardRepository.deleteByIdAndSetIdAndSetUserId(flashcardId, setId, userId);

        if (deletedCount == 0) {
            throw new BadRequestException("Flashcard not found or access denied.");
        }
    }

    @Override
    public FlashcardResponseDto updateFlashcard(Long setId, Long flashcardId, FlashcardUpdatingRequestDto flashcardUpdatingRequestDto) throws BadRequestException {
        Flashcard flashcard = flashcardRepository.getFlashcardByIdAndSetIdAndSetUserId(
                        flashcardId,
                        setId,
                        authenticationContext.getCurrentUserId()
                )
                .orElseThrow(() -> new BadRequestException("Flashcard not found or access denied."));

        flashcardMapper.updateFlashcardFromDto(flashcardUpdatingRequestDto, flashcard);
        Flashcard updatedFlashcard = flashcardRepository.save(flashcard);

        return flashcardMapper.toFlashcardResponseDto(updatedFlashcard);
    }

    @Override
    public DetailFlashcardResponseDto updateFlashcard(Flashcard newFlashcard) {
        return flashcardMapper.toDetailFlashcardResponseDto(flashcardRepository.save(newFlashcard));
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByFiles(GenerateFlashcardByFileRequestDto request) {
        List<MultipartFile> files = request.getFiles();

        StringBuilder allContent = new StringBuilder();

        for (MultipartFile file : files) {
            try {
                String fileName = file.getOriginalFilename();

                // Log thông tin file
                System.out.println("Processing file: " + fileName + " - Size: " + file.getSize());

                // Đọc nội dung file
                String content = fileService.readFile(file);

                // Thêm separator giữa các file
                allContent.append("=== Content from: ").append(fileName).append(" ===\n");
                allContent.append(content);
                allContent.append("\n\n");

            } catch (Exception e) {
                throw new RuntimeException("Error processing file: " + file.getOriginalFilename() + " - " + e.getMessage());
            }
        }

        String finalContent = allContent.toString();
        return aiFlashcardService.generateFlashcard(finalContent, "file");
    }

    @Override
    public GenerateFlashcardByAIResponseDto generateFlashcardByNotes(GenerateFlashcardByNoteRequestDto request) {
        List<Long> noteIds = request.getNoteIds();

        List<Note> notes = noteRepository.findAllById(noteIds);

        if (notes.isEmpty()) {
            throw new RuntimeException("No notes found with provided IDs");
        }

        StringBuilder allContent = new StringBuilder();
        for(Note note: notes){
            allContent.append("=== Note: ").append(note.getTitle()).append(" ===\n");
            allContent.append(note.getContent()).append("\n\n");
        }

        String finalContent = allContent.toString();
        System.out.println("Final content: " + finalContent);

        return aiFlashcardService.generateFlashcard(finalContent, "note");
    }

    @Override
    public Flashcard getFlashcardById(Long flashcardId) {
        return flashcardRepository.findById(flashcardId)
                .orElseThrow(() -> new ResourceNotFoundException("Flashcard with id " + flashcardId + " not found!"));
    }
}
