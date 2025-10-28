package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.ImageUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.UpdateUploadedImageRequestDto;
import com.cabybara.prolearningplatform.dto.response.ImageSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.ImageUrlUploadResponseDto;
import com.cabybara.prolearningplatform.service.upload.ImageAssetService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
@Validated
@Tag(name = "Image upload")
public class ImageUploadController {
    private final ImageAssetService imageAssetService;

    @Operation(
            summary = "Get signature to upload image (from file)",
            description = "This endpoint will return an (presigned signature) and the parameters. " +
                    "Client using these params to upload image directly to Cloudinary/S3. "
    )
    @GetMapping("/signature")
    public ResponseEntity<ApiResponse<ImageSignatureResponseDto>> getUploadSignature() {
        ImageSignatureResponseDto imageSignatureResponseDto = imageAssetService.generateUploadSignature();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", imageSignatureResponseDto, null));
    }

    @Operation(
            summary = "Upload image from url",
            description = "This endpoint used to upload image from url. " +
                    "It will return uploaded url and assetId (will be used after)"
    )
    @PostMapping("/upload-from-url")
    public ResponseEntity<ApiResponse<ImageUrlUploadResponseDto>> uploadFromUrl(
            @RequestBody ImageUrlUploadRequestDto request
    ) {
        ImageUrlUploadResponseDto imageUrlUploadResponseDto = imageAssetService.uploadImageFromUrl(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", imageUrlUploadResponseDto, null));
    }

    @Operation(
            summary = "Callback used to verify upload image successfully (from file)",
            description = "FORCE: After client upload file success (using /signature), " +
                    "client have to call this endpoint to send final URL (secure_url) returning from Cloudinart. "
    )
    @PostMapping("/update-uploaded-image")
    public ResponseEntity<ApiResponse<String>> updateUploadedImageSigned(
            @RequestBody UpdateUploadedImageRequestDto updateUploadedImageRequestDto
    ) {
        imageAssetService.updateUploadedImageSigned(updateUploadedImageRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", "Update uploaded image from signed url completed", null));
    }
}