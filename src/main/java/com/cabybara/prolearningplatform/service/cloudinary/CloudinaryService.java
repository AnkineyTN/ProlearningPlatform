package com.cabybara.prolearningplatform.service.cloudinary;

import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    public CloudinaryResponseDTO uploadMultipartFile(MultipartFile file, String fileName, String extension, String subject) throws IOException;
}