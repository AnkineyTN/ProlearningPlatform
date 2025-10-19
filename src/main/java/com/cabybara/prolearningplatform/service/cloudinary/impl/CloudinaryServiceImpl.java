package com.cabybara.prolearningplatform.service.cloudinary.impl;

import com.cabybara.prolearningplatform.dto.response.CloudinaryResponseDTO;
import com.cabybara.prolearningplatform.exception.UploadFileException;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
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

    // Folder for note
    private final String NOTE_DOCS_FOLDER = "ProLearning/note-documents";
    private final String NOTE_IMAGES_FOLDER = "ProLearning/note-images";

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
