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
public class SetUpdatingRequestDto {
    @Length(min = 3, max = 100)
    @Pattern(regexp = "^[\\p{L}\\p{Nd}\\s]+$", message = "Not contains special characters")
    @Schema(minLength = 3, maxLength = 100)
    private String title;

    @Length(max = 500)
    private String description;

    @NotNull
    private Privacy privacy;
}
