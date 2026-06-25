package com.cabybara.prolearningplatform.dto.response.exam;

import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.Privacy;
import lombok.Builder;

import java.time.OffsetDateTime;

@Builder
public record SharedExamResponseDto(
        Long id,
        String title,
        Privacy privacy,
        String description,
        Long duration,
        Long numQuestions,
        CreationMethod creationMethod,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        NoteRole userRole,
        Long setId,
        Boolean isFavorited,
        Long ownerId,
        String ownerName,
        String ownerAvatar) {}
