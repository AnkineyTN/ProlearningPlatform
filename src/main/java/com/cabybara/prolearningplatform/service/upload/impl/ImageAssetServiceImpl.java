package com.cabybara.prolearningplatform.service.upload.impl;

import com.cabybara.prolearningplatform.bean.AuthenticationContext;
import com.cabybara.prolearningplatform.dto.request.ImageUrlUploadRequestDto;
import com.cabybara.prolearningplatform.dto.request.UpdateUploadedImageRequestDto;
import com.cabybara.prolearningplatform.dto.response.ImageSignatureResponseDto;
import com.cabybara.prolearningplatform.dto.response.ImageUrlUploadResponseDto;
import com.cabybara.prolearningplatform.enums.ImageStatus;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.ImageAsset;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.repository.ImageAssetRepository;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.upload.ImageAssetService;
import com.cabybara.prolearningplatform.service.user.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
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
public class ImageAssetServiceImpl implements ImageAssetService {
    private final CloudinaryService cloudinaryService;
    private final UserService userService;
    private final ImageAssetRepository imageAssetRepository;
    private final AuthenticationContext authenticationContext;

    @Value("${cloud.cloudinary.flashcard_preset}")
    private String flashcardPreset;

    @Override
    @Transactional
    public ImageUrlUploadResponseDto uploadImageFromUrl(ImageUrlUploadRequestDto imageUrlUploadRequestDto) {
        Long currentUserId = authenticationContext.getCurrentUserId();
        User currentUser = userService.getUserById(currentUserId);

        try {
            String targetFolder = "prolearningplatform/flashcards";
            Map uploadResult = cloudinaryService.uploadImageFromUrl(imageUrlUploadRequestDto.getSourceUrl(), flashcardPreset);
            String finalUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            ImageAsset newAsset = ImageAsset.builder()
                    .publicId(publicId)
                    .url(finalUrl)
                    .status(ImageStatus.PENDING)
                    .user(currentUser)
                    .build();
            newAsset = imageAssetRepository.save(newAsset);

            return ImageUrlUploadResponseDto.builder()
                    .assetId(newAsset.getId())
                    .finalUrl(finalUrl)
                    .build();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload from URL", e);
        }
    }

    @Override
    @Transactional
    public ImageSignatureResponseDto generateUploadSignature() {
        Long currentUserId = authenticationContext.getCurrentUserId();
        User currentUser = userService.getUserById(currentUserId);
        String cloudinaryApiKey = cloudinaryService.getConfiguration().apiKey;
        String cloudinaryName = cloudinaryService.getConfiguration().cloudName;

        ImageAsset newAsset = ImageAsset.builder()
                .publicId("")
                .url("")
                .status(ImageStatus.PENDING)
                .user(currentUser)
                .build();
        newAsset = imageAssetRepository.save(newAsset);

        long timestamp = Instant.now().getEpochSecond();
        String signature = cloudinaryService.generateUploadSignature();

        return ImageSignatureResponseDto.builder()
                .signature(signature)
                .timestamp(timestamp)
                .apiKey(cloudinaryApiKey)
                .cloudName(cloudinaryName)
                .assetId(newAsset.getId())
                .uploadPreset(flashcardPreset)
                .build();
    }

    @Override
    @Transactional
    public Map<Long, ImageAsset> findAndActivateAssets(List<Long> assetIds, Long userId) {
        if (assetIds == null || assetIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<ImageAsset> assets = imageAssetRepository.findAllById(assetIds);

        if (assets.size() != assetIds.size()) {
            throw new ResourceNotFoundException("Some image asset not found!");
        }

        for (ImageAsset asset : assets) {
            if (!asset.getUser().getId().equals(userId)) {
                throw new SecurityException("User not have permission with assets: " + asset.getId());
            }
            asset.setStatus(ImageStatus.ACTIVE);
        }

        imageAssetRepository.saveAll(assets);

        return assets.stream()
                .collect(Collectors.toMap(ImageAsset::getId, asset -> asset));
    }

    @Override
    public ImageAsset findAndActivateAsset(Long assetId, Long userId) {
        if (assetId == null) {
            return null;
        }

        ImageAsset asset = imageAssetRepository.findById(assetId)
                .orElseThrow(() -> new ResourceNotFoundException("Image asset not found!"));

        if (!asset.getUser().getId().equals(userId)) {
            throw new SecurityException("User not have permission with assets: " + asset.getId());
        }
        asset.setStatus(ImageStatus.ACTIVE);

        imageAssetRepository.save(asset);

        return asset;
    }

    @Override
    @Transactional
    public void updateUploadedImageSigned(UpdateUploadedImageRequestDto updateUploadedImageRequestDto) {
        ImageAsset imageAsset = imageAssetRepository.findById(updateUploadedImageRequestDto.getAssetId())
                .orElseThrow(() -> new ResourceNotFoundException("Asset ID: " + updateUploadedImageRequestDto.getAssetId() + " not found."));

        imageAsset.setPublicId(updateUploadedImageRequestDto.getPublicId());
        imageAsset.setUrl(updateUploadedImageRequestDto.getUrl());

        imageAssetRepository.save(imageAsset);
    }

    @Override
    public List<ImageAsset> findAssetsToCleanup(OffsetDateTime cutoffTime) {
        List<ImageAsset> pending = imageAssetRepository.findByStatusAndCreatedAtBefore(
                ImageStatus.PENDING, cutoffTime);

        List<ImageAsset> deleted = imageAssetRepository.findByStatus(ImageStatus.DELETED);

        List<ImageAsset> cleanupList = new ArrayList<>(pending);
        cleanupList.addAll(deleted);
        return cleanupList;
    }

    @Override
    public void deleteAllAssets(List<ImageAsset> assets) {
        imageAssetRepository.deleteAll(assets);
    }

    @Override
    public void deleteImageAsset(ImageAsset imageAsset) {
        imageAsset.setStatus(ImageStatus.DELETED);
        imageAssetRepository.save(imageAsset);
    }
}
