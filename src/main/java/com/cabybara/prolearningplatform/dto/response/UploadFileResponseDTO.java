package com.cabybara.prolearningplatform.dto.response;

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
    String extension;
    String publicId;
}
