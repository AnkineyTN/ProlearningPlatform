package com.cabybara.prolearningplatform.service.impl;

import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import com.cabybara.prolearningplatform.exception.UploadFileException;
import com.cabybara.prolearningplatform.service.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Service
public class CloudinaryServiceImpl implements CloudinaryService {
    @Autowired
    private Cloudinary cloudinary;

    private final String NOTE_DOCS_FOLDER = "ProLearning/note-documents";

    @Override
    public CloudinaryResponseDTO uploadMultipartFile(MultipartFile file, String fileName, String extension, String subject) throws IOException {
        log.info("Upload multipart files");

        // TODO: Handle for many type (note, flashcard, test,...)
        String folder = "";

        if (subject.equals("note")) {
            folder = NOTE_DOCS_FOLDER;
        }

        // Handle upload correct file type
        String type = "";
        if (extension.equals("txt") || extension.equals("pdf") || extension.equals("docx") || extension.equals("pptx")) {
            type = "raw";
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
}
