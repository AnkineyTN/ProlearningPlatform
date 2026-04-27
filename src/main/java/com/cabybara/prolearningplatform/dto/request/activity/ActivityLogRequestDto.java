package com.cabybara.prolearningplatform.dto.request.activity;

import com.cabybara.prolearningplatform.enums.ContentType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ActivityLogRequestDto {

    @NotNull
    private ContentType contentType;

    private Long setId;

    private Long todoId;

    @NotNull
    @Min(0)
    private Long activeDuration;

    @NotNull
    @Min(0)
    private Long rawDuration;

    private Integer score;

    private Integer itemsCount;

    @NotNull
    private String clientTimestamp;
}
