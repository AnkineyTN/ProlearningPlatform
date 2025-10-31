package com.cabybara.prolearningplatform.dto.response;

import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.FlashcardStatus;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.OffsetDateTime;
import java.util.List;

@Data
@SuperBuilder
public class FlashcardResponseDto {
    private String id;
    private String title;
    private String description;
    private FlashcardStatus status;
    private Privacy privacy;
    private OffsetDateTime lastStudy;
    private Integer known;
    private Integer learning;
    private Integer remain;

    private CreationMethod createMethod;
}
