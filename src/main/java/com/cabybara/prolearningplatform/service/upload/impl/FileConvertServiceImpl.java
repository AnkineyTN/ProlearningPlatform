package com.cabybara.prolearningplatform.service.upload.impl;

import com.aspose.words.Document;
import com.aspose.words.SaveFormat;
import com.cabybara.prolearningplatform.service.upload.FileConvertService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class FileConvertServiceImpl implements FileConvertService {
    private static final String TEMP_DIR = "temp";

    @Override
    public File convertToPDF(MultipartFile file, String extension) throws IOException {
        switch (extension) {
            case "pdf":
                return convertMultipartFileToFile(file, extension);
            case "doc":
            case "docx":
                return convertWordToPDF(file, extension);
            default:
                throw new IOException("Unsupported file type for PDF conversion: " + extension);
        }
    }

    private File convertMultipartFileToFile(MultipartFile file, String ext) throws IOException {
        File tempDir = getTempDir();
        File convFile = File.createTempFile("temp-", "." + ext, tempDir);
        file.transferTo(convFile);
        return convFile;
    }

    private File convertWordToPDF(MultipartFile file, String ext) throws IOException {
        File tempDir = getTempDir();
        File pdfFile = File.createTempFile("converted-", ".pdf", tempDir);
        try {
            // Use Aspose
            Document doc = new Document(file.getInputStream());
            doc.save(pdfFile.getAbsolutePath(), SaveFormat.PDF);

            return pdfFile;
        } catch (Exception e) {
            throw new IOException("Error converting Word to PDF", e);
        }
    }

    private File getTempDir() throws IOException {
        File tempDir = new File(TEMP_DIR);
        if (!tempDir.exists()) {
            boolean created = tempDir.mkdirs();
            if (!created) {
                throw new IOException("Could not create temp directory: " + TEMP_DIR);
            }
        }
        return tempDir;
    }
}
