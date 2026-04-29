package com.cabybara.prolearningplatform.dto.request.pomodoro;

import org.hibernate.validator.constraints.URL;

import com.cabybara.prolearningplatform.enums.AssetType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSystemSpaceRequestDto {
    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String publicId;

    @NotBlank @URL
    private String url;

    @NotNull
    private AssetType assetType;
}