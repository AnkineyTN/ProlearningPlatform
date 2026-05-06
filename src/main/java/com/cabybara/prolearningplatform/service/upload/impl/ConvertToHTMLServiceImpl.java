package com.cabybara.prolearningplatform.service.upload.impl;

import com.cabybara.prolearningplatform.service.upload.ConvertToHTMLService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConvertToHTMLServiceImpl implements ConvertToHTMLService {
    private final String PDF_TO_HTML_API = "https://prolearning-pdftohtmlconverter.onrender.com/convert?url=";

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String convertToHTML(String url) throws IOException {
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL is null or empty");
        }

        try {
            String apiUrl = PDF_TO_HTML_API + url;
            log.info("Calling PDF to HTML API: {}", apiUrl);

            String htmlContent = restTemplate.getForObject(apiUrl, String.class);

            if (htmlContent == null || htmlContent.isEmpty()) {
                throw new IOException("Empty response from PDF to HTML API");
            }

//            log.info("PDF to HTML API response: {}", htmlContent);
            return htmlContent;
        } catch (Exception e) {
            log.error("Failed to convert PDF to HTML", e);
            throw new IOException("PDF to HTML conversion failed: " + e.getMessage(), e);
        }
    }
}
