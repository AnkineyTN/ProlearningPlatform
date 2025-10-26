package com.cabybara.prolearningplatform.service.cloudinary.impl;

import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import com.cabybara.prolearningplatform.exception.UploadFileException;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Configuration;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    @Value("${cloud.cloudinary.flashcard_preset}")
    private String flashcardPreset;

    // Folder for note
    private final String NOTE_DOCS_FOLDER = "ProLearning/note-documents";
    private final String NOTE_IMAGES_FOLDER = "ProLearning/note-images";

    @Override
    public String generateUploadSignature() {
        long timestamp = Instant.now().getEpochSecond();

        Map<String, Object> paramsToSign = Map.of(
                "timestamp", timestamp,
                "upload_preset", flashcardPreset
        );

        return cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);
    }

    @Override
    public Configuration getConfiguration() {
        return cloudinary.config;
    }

    @Override
    public Map uploadImageFromUrl(String imageUrl, String targetFolder) throws IOException {
        if (imageUrl == null || imageUrl.trim().isEmpty()) {
            throw new IllegalArgumentException("Source URL cannot be null or empty.");
        }

        Map options = ObjectUtils.asMap(
                "upload_preset", flashcardPreset,
                "resource_type", "image",
                "overwrite", false
        );

        return cloudinary.uploader().upload(
                imageUrl,
                options
        );
    }

    @Override
    public CloudinaryResponseDTO uploadMultipartFile(MultipartFile file, String fileName, String extension, String subject) throws IOException {
        log.info("Upload multipart files");

        // Handle upload to correct folder on Cloudinary
        String folder = "";
        if (subject.equals("note-document")) {
            folder = NOTE_DOCS_FOLDER;
        } else if (subject.equals("note-image")) {
            folder = NOTE_IMAGES_FOLDER;
        }

        // Handle upload correct file type
        String type = "";
        if (extension.equals("txt") || extension.equals("pdf") || extension.equals("docx") || extension.equals("pptx")) {
            type = "raw";
        } else if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
            type = "image";
        }

        Map<?, ?> result;
        try {
            result = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", fileName,
                            "resource_type", type,
                            "folder", folder,
                            "overwrite", true,
                            "invalidate", true
                    )
            );
        } catch (RuntimeException e) {
            throw new UploadFileException(e.getMessage());
        }

        String fileUrl = result.get("secure_url").toString();
        String publicId = result.get("public_id").toString();

        return CloudinaryResponseDTO.builder()
                .fileUrl(fileUrl)
                .publicId(publicId)
                .build();
    }

    @Override
    public void deleteFile(String publicId, String extension) throws IOException {
        String type = "";
        if (extension.equals("txt") || extension.equals("pdf") || extension.equals("docx") || extension.equals("pptx")) {
            type = "raw";
        } else if (extension.equals("jpg") || extension.equals("jpeg") || extension.equals("png")) {
            type = "image";
        }
        cloudinary.uploader().destroy(publicId, ObjectUtils.asMap(
                "resource_type", type
        ));
    }
}
