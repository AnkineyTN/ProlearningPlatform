package com.cabybara.prolearningplatform.service.asset;

import com.cabybara.prolearningplatform.dto.request.common.AssetUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.notification.UpdateUploadedAssetRequestDto;
import com.cabybara.prolearningplatform.dto.response.common.AssetSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.common.AssetUrlUploadResponseDto;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cabybara.prolearningplatform.model.Asset;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

public interface AssetService {
    AssetSignatureResponseDto generateUploadSignature(AssetType assetType);

    AssetUrlUploadResponseDto uploadAssetFromUrl(AssetUrlUploadRequestDto request);

    Map<Long, Asset> findAndActivateAssets(List<Long> assetIds, Long userId);

    Asset findAndActivateAsset(Long assetId, Long userId);

    void updateUploadedAssetSigned(UpdateUploadedAssetRequestDto updateUploadedAssetRequestDto);

    List<Asset> findAssetsToCleanup(OffsetDateTime cutoffTime);

    void deleteAllAssets(List<Asset> assets);

    void markDeletedAsset(Asset asset);
}
