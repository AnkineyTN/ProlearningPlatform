package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.response.ResponseData;
import com.cabybara.prolearningplatform.dto.response.ResponseError;
import com.cabybara.prolearningplatform.dto.response.UploadFileResponseDTO;
import com.cabybara.prolearningplatform.service.upload.UploadFileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload-file")
@Validated
@Slf4j
@Tag(name = "Upload File APIs")
@RequiredArgsConstructor
public class UploadFileController {
    private final UploadFileService uploadFileService;

    private static final String ERROR_MESSAGE = "errorMessage={}";

    @Operation(
            method = "POST",
            summary = "Upload file",
            description = """
                    Upload file from Notes, Flashcards, Tests, Mindmaps, Recaps
                    
                    📝 Form-data fields:
                    - file: multipart file to upload (document, image)
                    - subject: string
                        + Notes: Document (note-document), Image (note-image)
                        + ...
                    
                    📌 Note:
                    - Max file size is limited by server config.
                    - Document types accept: .pdf, .docx, .txt, .pptx
                    - Image types accept: .jpg, .jpeg, .png
                    """
    )
    @PostMapping(value = "")
    public ResponseData<UploadFileResponseDTO> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("subject") String subject, @RequestParam("id") Long id) {
        log.info("Upload file (Document or Image)");
        try {
            return new ResponseData<>(HttpStatus.OK.value(), "Upload file", uploadFileService.uploadFile(file, subject, id));
        } catch (Exception e) {
            log.error(ERROR_MESSAGE, e.getMessage(), e.getCause());
            return new ResponseError<>(HttpStatus.BAD_REQUEST.value(), "Upload file fail");
        }
    }
}
