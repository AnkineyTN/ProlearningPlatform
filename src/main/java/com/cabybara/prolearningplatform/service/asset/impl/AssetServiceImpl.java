package com.cabybara.prolearningplatform.service.asset.impl;

import com.cabybara.prolearningplatform.dto.request.common.AssetUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.common.UpdateUploadedAssetRequestDto;
import com.cabybara.prolearningplatform.dto.response.common.AssetSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.common.AssetUrlUploadResponseDto;
import com.cabybara.prolearningplatform.enums.AssetStatus;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.AssetRepository;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssetServiceImpl implements AssetService {
    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################
    private final AssetRepository assetRepository;
    private final AuthenticationContext authenticationContext;

    private final CloudinaryService cloudinaryService;
    private final UserService userService;

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################
    @Override
    @Transactional
    public AssetSignatureResponseDto generateUploadSignature(AssetType assetType) {
        Long currentUserId = authenticationContext.getCurrentUserId();
        User currentUser = userService.getUserById(currentUserId);
        String cloudinaryApiKey = cloudinaryService.getConfiguration().apiKey;
        String cloudinaryName = cloudinaryService.getConfiguration().cloudName;

        Asset newAsset = Asset.builder()
                .publicId("")
                .url("")
                .status(AssetStatus.PENDING)
                .type(assetType)
                .user(currentUser)
                .build();
        newAsset = assetRepository.save(newAsset);

        long timestamp = Instant.now().getEpochSecond();
        Map signInfo = cloudinaryService.generateUploadSignature(assetType);

        return AssetSignatureResponseDto.builder()
                .signature(signInfo.get("signature").toString())
                .timestamp(timestamp)
                .apiKey(cloudinaryApiKey)
                .cloudName(cloudinaryName)
                .assetId(newAsset.getId())
                .uploadPreset(signInfo.get("uploadPreset").toString())
                .uploadResourceType(signInfo.get("uploadResourceType").toString())
                .build();
    }

    @Override
    @Transactional
    public AssetUrlUploadResponseDto uploadAssetFromUrl(AssetUrlUploadRequestDto assetUrlUploadRequestDto) {
        Long currentUserId = authenticationContext.getCurrentUserId();
        User currentUser = userService.getUserById(currentUserId);

        try {
            Map uploadResult = cloudinaryService.uploadResourceFromUrl(assetUrlUploadRequestDto.getSourceUrl(), assetUrlUploadRequestDto.getAssetType());
            String finalUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            Asset newAsset = Asset.builder()
                    .publicId(publicId)
                    .url(finalUrl)
                    .status(AssetStatus.PENDING)
                    .type(assetUrlUploadRequestDto.getAssetType())
                    .user(currentUser)
                    .build();
            newAsset = assetRepository.save(newAsset);

            return AssetUrlUploadResponseDto.builder()
                    .assetId(newAsset.getId())
                    .finalUrl(finalUrl)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload from URL", e);
        }
    }

    @Override
    @Transactional
    public void updateUploadedAssetSigned(UpdateUploadedAssetRequestDto updateUploadedAssetRequestDto) {
        Asset asset = assetRepository.findById(updateUploadedAssetRequestDto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset ID: " + updateUploadedAssetRequestDto.getAssetId() + " not found."));

        String url = updateUploadedAssetRequestDto.getUrl();
        asset.setPublicId(updateUploadedAssetRequestDto.getPublicId());
        asset.setStatus(AssetStatus.ACTIVE);
        asset.setUrl(url);
        asset.setFileName(updateUploadedAssetRequestDto.getFileName());

        assetRepository.save(asset);
    }

    @Override
    @Transactional
    public Map<Long, Asset> findAndActivateAssets(List<Long> assetIds, Long userId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Asset> assets = assetRepository.findAllById(assetIds);

        if (assets.size() != assetIds.size()) {
            throw new ResourceNotFoundException("Some asset asset not found!");
        }

        for (Asset asset : assets) {
            if (!asset.getUser().getId().equals(userId)) {
                throw new SecurityException("User not have permission with assets: " + asset.getId());
            }
            asset.setStatus(AssetStatus.ACTIVE);
        }

        assetRepository.saveAll(assets);

        return assets.stream()
                .collect(Collectors.toMap(Asset::getId, asset -> asset));
    }

    @Override
    public Asset findAndActivateAsset(Long assetId, Long userId) {
        if (assetId == null) {
            return null;
        }

        Asset asset = assetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Asset not found!"));

        if (!asset.getUser().getId().equals(userId)) {
            throw new SecurityException("User not have permission with assets: " + asset.getId());
        }
        asset.setStatus(AssetStatus.ACTIVE);

        assetRepository.save(asset);

        return asset;
    }

    @Override
    public List<Asset> findAssetsToCleanup(OffsetDateTime cutoffTime) {
        List<Asset> pendings = assetRepository.findByStatusAndCreatedAtBefore(
                AssetStatus.PENDING, cutoffTime);

        List<Asset> deleteds = assetRepository.findByStatusAndCreatedAtBefore(AssetStatus.DELETED, cutoffTime);

        List<Asset> cleanupList = new ArrayList<>();
        cleanupList.addAll(pendings);
        cleanupList.addAll(deleteds);
        return cleanupList;
    }

    @Override
    public void deleteAllAssets(List<Asset> assets) {
        assetRepository.deleteAll(assets);
    }

    @Override
    public void markDeletedAsset(Asset asset) {
        asset.setStatus(AssetStatus.DELETED);
        assetRepository.save(asset);
    }

    @Override
    public void markActiveAsset(Asset asset) {
        asset.setStatus(AssetStatus.ACTIVE);
        assetRepository.save(asset);
    }

    @Override
    @Transactional
    public AssetSignatureResponseDto generateSystemAssetSignature(AssetType assetType) {
        String cloudinaryApiKey = cloudinaryService.getConfiguration().apiKey;
        String cloudinaryName   = cloudinaryService.getConfiguration().cloudName;

        Asset newAsset = Asset.builder()
                .publicId("")
                .url("")
                .status(AssetStatus.PENDING)
                .type(assetType)
                .user(null)           
                .build();
        newAsset = assetRepository.save(newAsset);

        long timestamp = Instant.now().getEpochSecond();
        Map signInfo = cloudinaryService.generateUploadSignature(assetType);

        return AssetSignatureResponseDto.builder()
                .signature(signInfo.get("signature").toString())
                .timestamp(timestamp)
                .apiKey(cloudinaryApiKey)
                .cloudName(cloudinaryName)
                .assetId(newAsset.getId())
                .uploadPreset(signInfo.get("uploadPreset").toString())
                .uploadResourceType(signInfo.get("uploadResourceType").toString())
                .build();
    }
}
