package com.cabybara.prolearningplatform.service.cloudinary;

import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import com.cloudinary.Configuration;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

public interface CloudinaryService {
    String generateUploadSignature();

    Configuration getConfiguration();

    Map uploadImageFromUrl(String imageUrl, String targetFolder) throws IOException;

    public CloudinaryResponseDTO uploadMultipartFile(MultipartFile file, String fileName, String extension, String subject) throws IOException;

    public void deleteFile(String publicId, String type) throws IOException;
}