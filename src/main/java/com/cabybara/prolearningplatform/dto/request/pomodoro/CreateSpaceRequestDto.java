package com.cabybara.prolearningplatform.dto.request.pomodoro;

import com.google.firebase.database.annotations.NotNull;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSpaceRequestDto {
    @NotBlank
    private String name;

    private String description;

    @NotNull
    private Long assetId;
}