package com.cabybara.prolearningplatform.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface CloudinaryService {
    public String uploadMultipartFile(MultipartFile file, String fileName, String type, String subject) throws IOException;
}