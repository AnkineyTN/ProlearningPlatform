package com.cabybara.prolearningplatform.service;

import com.cabybara.prolearningplatform.dto.response.UploadFileResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface UploadFileService {
    public UploadFileResponseDTO uploadDocument(MultipartFile file, String subject, Long noteId) throws IOException;
}
