package com.cabybara.prolearningplatform.service.upload;

import com.cabybara.prolearningplatform.dto.response.common.UploadFileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadFileService {
    public UploadFileResponseDTO uploadFile(MultipartFile file, String subject, Long id) throws IOException;
}
