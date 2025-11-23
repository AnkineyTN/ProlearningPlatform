package com.cabybara.prolearningplatform.service.upload;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Service
public interface FileConvertService {
    public File convertToPDF(MultipartFile file, String extension) throws IOException;
}
