package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.ImageUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.UpdateUploadedImageRequestDto;
import com.cabybara.prolearningplatform.dto.response.ImageSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.ImageUrlUploadResponseDto;
import com.cabybara.prolearningplatform.service.upload.ImageUploadService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Validated
public class ImageUploadController {
    private final ImageUploadService imageUploadService;

    @GetMapping("/signature")
    public ResponseEntity<ApiResponse<?>> getUploadSignature() {
        ImageSignatureResponseDto imageSignatureResponseDto = imageUploadService.generateUploadSignature();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", imageSignatureResponseDto, null));
    }

    @PostMapping("/upload-from-url")
    public ResponseEntity<ApiResponse<?>> uploadFromUrl(
            @RequestBody ImageUrlUploadRequestDto request
    ) {
        ImageUrlUploadResponseDto imageUrlUploadResponseDto = imageUploadService.uploadImageFromUrl(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", imageUrlUploadResponseDto, null));
    }

    @PostMapping("/update-uploaded-image")
    public ResponseEntity<ApiResponse<?>> updateUploadedImageSigned(
            @RequestBody UpdateUploadedImageRequestDto updateUploadedImageRequestDto
    ) {
        imageUploadService.updateUploadedImageSigned(updateUploadedImageRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", "Update uploaded image from signed url completed", null));
    }
}