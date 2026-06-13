package com.cabybara.prolearningplatform.service.favorite.impl;

import com.cabybara.prolearningplatform.dto.response.social.SocialItemResponseDto;
import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.UserFavoriteResource;
import com.cabybara.prolearningplatform.repository.*;
import com.cabybara.prolearningplatform.service.favorite.UserFavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserFavoriteServiceImpl implements UserFavoriteService {

    private final UserFavoriteResourceRepository userFavoriteResourceRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;

    @Override
    @Transactional
    public boolean toggleFavorite(Long userId, Long resourceId, ContentType resourceType) {
        Optional<UserFavoriteResource> existingFavorite = userFavoriteResourceRepository
                .findByUserIdAndResourceIdAndResourceType(userId, resourceId, resourceType);

        if (existingFavorite.isPresent()) {
            userFavoriteResourceRepository.delete(existingFavorite.get());
            return false;
        }

        // Validate if resource exists
        boolean resourceExists = false;
        switch (resourceType) {
            case NOTE -> resourceExists = noteRepository.existsById(resourceId);
            case FLASHCARD -> resourceExists = flashcardRepository.existsById(resourceId);
            case EXAM -> resourceExists = examRepository.existsById(resourceId);
        }

        if (!resourceExists) {
            throw new com.cabybara.prolearningplatform.exception.ResourceNotFoundException("Resource not found");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.cabybara.prolearningplatform.exception.ResourceNotFoundException("User not found"));

        UserFavoriteResource favoriteResource = UserFavoriteResource.builder()
                .user(user)
                .resourceId(resourceId)
                .resourceType(resourceType)
                .build();

        userFavoriteResourceRepository.save(favoriteResource);
        return true;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SocialItemResponseDto> getFavoriteResources(Long userId, String q, ContentType resourceType, Pageable pageable) {
        Page<UserFavoriteResource> favoritePage = userFavoriteResourceRepository.searchFavoriteResources(userId, resourceType, q, pageable);

        List<SocialItemResponseDto> resultList = new ArrayList<>();

        for (UserFavoriteResource fav : favoritePage.getContent()) {
            Long rId = fav.getResourceId();
            ContentType type = fav.getResourceType();

            if (type == ContentType.NOTE) {
                noteRepository.findById(rId).ifPresent(note -> {
                    String ownerName = note.getUser() != null ? buildOwnerName(note.getUser().getFirstName(), note.getUser().getLastName()) : "";
                    String avatarUrl = note.getUser() != null ? note.getUser().getAvatarUrl() : null;
                    Long ownerId = note.getUser() != null ? note.getUser().getId() : null;
                    Long setId = note.getSet() != null ? note.getSet().getId() : null;
                    
                    resultList.add(new SocialItemResponseDto(
                            note.getId(), setId, "NOTE", note.getTitle(), note.getDescription(),
                            note.getCreatedAt(), note.getUpdatedAt(), ownerId, ownerName, avatarUrl, null, null, null, true
                    ));
                });
            } else if (type == ContentType.FLASHCARD) {
                flashcardRepository.findById(rId).ifPresent(fc -> {
                    String ownerName = fc.getUser() != null ? buildOwnerName(fc.getUser().getFirstName(), fc.getUser().getLastName()) : "";
                    String avatarUrl = fc.getUser() != null ? fc.getUser().getAvatarUrl() : null;
                    Long ownerId = fc.getUser() != null ? fc.getUser().getId() : null;
                    Long setId = fc.getSet() != null ? fc.getSet().getId() : null;
                    long numCards = fc.getCards() != null ? fc.getCards().size() : 0L;
                    
                    resultList.add(new SocialItemResponseDto(
                            fc.getId(), setId, "FLASHCARD", fc.getTitle(), fc.getDescription(),
                            fc.getCreatedAt(), fc.getUpdatedAt(), ownerId, ownerName, avatarUrl, numCards, null, null, true
                    ));
                });
            } else if (type == ContentType.EXAM) {
                examRepository.findById(rId).ifPresent(exam -> {
                    String ownerName = "";
                    String avatarUrl = null;
                    Long ownerId = exam.getCreatedBy();
                    
                    if (ownerId != null) {
                        var optUser = userRepository.findById(ownerId);
                        if (optUser.isPresent()) {
                            ownerName = buildOwnerName(optUser.get().getFirstName(), optUser.get().getLastName());
                            avatarUrl = optUser.get().getAvatarUrl();
                        }
                    }
                    Long setId = exam.getSet() != null ? exam.getSet().getId() : null;
                    long numQuestions = exam.getExamQuestions() != null ? exam.getExamQuestions().size() : 0L;
                    
                    resultList.add(new SocialItemResponseDto(
                            exam.getId(), setId, "EXAM", exam.getTitle(), exam.getDescription(),
                            exam.getCreatedAt(), exam.getUpdatedAt(), ownerId, ownerName, avatarUrl, null, numQuestions, exam.getDuration(), true
                    ));
                });
            }
        }

        return new PageImpl<>(resultList, pageable, favoritePage.getTotalElements());
    }

    private String buildOwnerName(String firstName, String lastName) {
        return String.format("%s %s",
                Objects.requireNonNullElse(firstName, ""),
                Objects.requireNonNullElse(lastName, "")
        ).trim();
    }
}
