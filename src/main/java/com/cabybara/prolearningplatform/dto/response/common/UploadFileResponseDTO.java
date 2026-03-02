package com.cabybara.prolearningplatform.dto.response.common;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UploadFileResponseDTO {
    Long id;
    String fileName;
    String fileUrl;
    String content;
    String extension;
    String publicId;
}
