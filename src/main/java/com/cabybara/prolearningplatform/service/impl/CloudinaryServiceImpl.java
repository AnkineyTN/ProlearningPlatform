package com.cabybara.prolearningplatform.service.impl;

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

    @Override
    public String uploadMultipartFile(MultipartFile file, String fileName, String type, String subject) throws IOException {
        log.info("Upload multipart files");

        // TODO: Handle for many type (note, flashcard, test,...)
        String folder = "";
        if(type.equals("note")){
            folder = "note-documents";
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
        return result.get("secure_url").toString();
    }
}
