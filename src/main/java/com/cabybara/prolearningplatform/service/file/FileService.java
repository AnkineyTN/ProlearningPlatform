package com.cabybara.prolearningplatform.service.file;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {
    public String readFile(MultipartFile file);
}
