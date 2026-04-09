package com.cabybara.prolearningplatform.dto.request.note;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RectPercentRequestDTO {

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double x;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double y;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double width;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private Double height;
}
