package com.cabybara.prolearningplatform.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.io.File;

@Data
@Builder
public class CardItemCreateRequestDto {
    @NotNull
    @NotEmpty
    @NotBlank
    private String frontCard;

    @NotNull
    @NotEmpty
    @NotBlank
    private String backCard;

    private Long imageAssetId;
}
