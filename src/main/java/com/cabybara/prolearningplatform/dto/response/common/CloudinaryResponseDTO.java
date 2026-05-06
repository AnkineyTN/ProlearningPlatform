package com.cabybara.prolearningplatform.dto.response.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class CloudinaryResponseDTO {
    String publicId;
    String fileUrl;
}
