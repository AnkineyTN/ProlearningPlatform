package com.cabybara.prolearningplatform.service.upload;

import com.cabybara.prolearningplatform.dto.request.ImageUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.UpdateUploadedImageRequestDto;
import com.cabybara.prolearningplatform.dto.response.ImageSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.ImageUrlUploadResponseDto;
import com.cabybara.prolearningplatform.model.ImageAsset;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface ImageAssetService {
    ImageSignatureResponseDto generateUploadSignature();

    ImageUrlUploadResponseDto uploadImageFromUrl(ImageUrlUploadRequestDto request);

    Map<Long, ImageAsset> findAndActivateAssets(List<Long> assetIds, Long userId);

    ImageAsset findAndActivateAsset(Long assetId, Long userId);

    void updateUploadedImageSigned(UpdateUploadedImageRequestDto updateUploadedImageRequestDto);

    List<ImageAsset> findAssetsToCleanup(OffsetDateTime cutoffTime);

    void deleteAllAssets(List<ImageAsset> assets);

    void deleteImageAsset(ImageAsset imageAsset);
}
