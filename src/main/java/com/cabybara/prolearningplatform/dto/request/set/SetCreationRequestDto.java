package com.cabybara.prolearningplatform.dto.request.set;

import com.cabybara.prolearningplatform.enums.Privacy;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
@Builder
public class SetCreationRequestDto {
    @NotNull
    @Length(min = 3, max = 50)
    @Schema(minLength = 3, maxLength = 50)
    @Pattern(regexp = "^[\\p{L}\\p{Nd}\\s]+$", message = "Not contains special characters")
    private String title;
    private String description;
    private Privacy privacy;
}

