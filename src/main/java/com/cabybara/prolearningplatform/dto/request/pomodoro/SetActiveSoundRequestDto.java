package com.cabybara.prolearningplatform.dto.request.pomodoro;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SetActiveSoundRequestDto {
    @NotNull
    private Long soundId;

    @NotNull 
    @DecimalMin("0.0") 
    @DecimalMax("1.0")
    private Float volume;
}