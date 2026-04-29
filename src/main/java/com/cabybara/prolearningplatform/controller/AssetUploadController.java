package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.common.AssetUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.common.UpdateUploadedAssetRequestDto;
import com.cabybara.prolearningplatform.dto.response.common.AssetSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.common.AssetUrlUploadResponseDto;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/assets")
@RequiredArgsConstructor
@Validated
@Tag(name = "Asset upload")
public class AssetUploadController {
    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################
    private final AssetService assetService;

    // ##################################################
    // ###################  MAIN API  ###################
    // ##################################################
    @Operation(
            summary = "Get signature to upload asset (from file)",
            description = "This endpoint will return an (presigned signature) and the parameters. " +
                    "Client using these params to upload file directly to Cloudinary/S3. "
    )
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/signature/{type}")
    public ResponseEntity<ApiResponse<AssetSignatureResponseDto>> getUploadSignature(
            @PathVariable AssetType type
    ) {
        AssetSignatureResponseDto assetSignatureResponseDto = assetService.generateUploadSignature(type);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", assetSignatureResponseDto, null));
    }

    @Operation(
            summary = "Upload file from url",
            description = "This endpoint used to upload file from url. " +
                    "It will return uploaded url and assetId (will be used after)"
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/upload-from-url")
    public ResponseEntity<ApiResponse<AssetUrlUploadResponseDto>> uploadFromUrl(
            @RequestBody AssetUrlUploadRequestDto request
    ) {
        AssetUrlUploadResponseDto assetUrlUploadResponseDto = assetService.uploadAssetFromUrl(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", assetUrlUploadResponseDto, null));
    }

    @Operation(
            summary = "Callback used to verify upload asset successfully (from file)",
            description = "FORCE: After client upload file success (using /signature), " +
                    "client have to call this endpoint to send final URL (secure_url) returning from Cloudinary. "
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/update-uploaded-asset")
    public ResponseEntity<ApiResponse<String>> updateUploadedImageSigned(
            @RequestBody UpdateUploadedAssetRequestDto updateUploadedAssetRequestDto
    ) {
        assetService.updateUploadedAssetSigned(updateUploadedAssetRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Successfully", "Update uploaded image from signed url completed", null));
    }

    @GetMapping("/signature/system")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<AssetSignatureResponseDto> generateSystemSignature(
        @RequestParam AssetType type) {
        return ResponseEntity.ok(assetService.generateSystemAssetSignature(type));
   }
}