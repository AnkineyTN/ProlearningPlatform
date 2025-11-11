package com.cabybara.prolearningplatform.service.upload;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ConvertFileService {
    public String convertToHTML(String url) throws IOException;
}

