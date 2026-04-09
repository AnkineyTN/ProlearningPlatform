package com.cabybara.prolearningplatform.dto.response.note;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RectPercentResponseDTO {
    private double x;
    private double y;
    private double width;
    private double height;
}
