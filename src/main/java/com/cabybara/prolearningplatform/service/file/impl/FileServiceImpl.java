package com.cabybara.prolearningplatform.service.file.impl;

import com.cabybara.prolearningplatform.service.file.FileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFShape;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {
    @Override
    public String readFile(MultipartFile file) {
        try {
            String fileName = file.getOriginalFilename();

            // Kiểm tra null
            if (fileName == null) {
                throw new RuntimeException("File name is null");
            }

            if (fileName.endsWith(".txt")) {
                return readTxt(file);
            } else if (fileName.endsWith(".pdf")) {
                return readPdf(file);
            } else if (fileName.endsWith(".docx")) {
                return readDocx(file);
            } else if (fileName.endsWith(".pptx")) {
                return readPptx(file);

            } else {
                throw new RuntimeException("Unsupported file type: " + fileName);
            }

        } catch (Exception e) {
            throw new RuntimeException("Error reading file: " + e.getMessage(), e);
        }
    }

    // == TXT
    private String readTxt(MultipartFile file) throws IOException {
        return new String(file.getBytes(), StandardCharsets.UTF_8);
    }

    // == PDF
    private String readPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    // == DOCX
    private String readDocx(MultipartFile file) throws IOException {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
             XWPFWordExtractor extractor = new XWPFWordExtractor(doc)) {
            return extractor.getText();
        }
    }

    // == PPTX
    private String readPptx(MultipartFile file) throws IOException {
        try (XMLSlideShow ppt = new XMLSlideShow(file.getInputStream())) {
            StringBuilder sb = new StringBuilder();

            for (XSLFSlide slide : ppt.getSlides()) {
                for (XSLFShape shape : slide.getShapes()) {
                    if (shape instanceof XSLFTextShape textShape) {
                        sb.append(textShape.getText()).append("\n");
                    }
                }
            }

            return sb.toString();
        }
    }
}
