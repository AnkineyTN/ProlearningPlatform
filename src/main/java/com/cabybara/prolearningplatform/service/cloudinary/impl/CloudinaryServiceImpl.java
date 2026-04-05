package com.cabybara.prolearningplatform.service.cloudinary.impl;

import com.cabybara.prolearningplatform.dto.helper.AssetToDeleteDto;
import com.cabybara.prolearningplatform.dto.response.common.CloudinaryResponseDTO;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cabybara.prolearningplatform.exception.UploadFileException;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cloudinary.Cloudinary;
import com.cloudinary.Configuration;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CloudinaryServiceImpl implements CloudinaryService {
    // ##################################################
    // #################  PREPARATION  ##################
    // ##################################################
    private final Cloudinary cloudinary;

    @Value("${cloud.cloudinary.image_preset}")
    private String imagePreset;

    @Value("${cloud.cloudinary.document_preset}")
    private String documentPreset;

    private final String NOTE_DOCS_FOLDER = "ProLearning/note-documents";
    private final String NOTE_IMAGES_FOLDER = "ProLearning/note-images";

    // ##################################################
    // #################  MAIN METHOD  ##################
    // ##################################################
    @Override
    public Configuration getConfiguration() {
        return cloudinary.config;
    }

    @Override
    public Map generateUploadSignature(AssetType type) {
        long timestamp = Instant.now().getEpochSecond();

        String selectedPreset = type == AssetType.IMAGE ? imagePreset : documentPreset;
        String selectedResourceType = type == AssetType.IMAGE ? "image" : "raw";

        Map<String, Object> paramsToSign = Map.of(
                "timestamp", timestamp,
                "upload_preset", selectedPreset
        );

        String signature = cloudinary.apiSignRequest(paramsToSign, cloudinary.config.apiSecret);

        return ObjectUtils.asMap(
                "signature", signature,
                "uploadPreset", selectedPreset,
                "uploadResourceType", selectedResourceType
        );
    }

    @Override
    public Map uploadResourceFromUrl(String url, AssetType type) throws IOException {
        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("Source URL cannot be null or empty.");
        }

        String targetPreset;
        String resourceType;

        if (type == AssetType.DOCUMENT) {
            targetPreset = documentPreset;
            resourceType = "auto";
        } else {
            targetPreset = imagePreset;
            resourceType = "image";
        }

        Map options = ObjectUtils.asMap(
                "upload_preset", targetPreset,
                "resource_type", resourceType,
                "overwrite", false
        );

        try {
            return cloudinary.uploader().upload(url, options);
        } catch (Exception e) {
            throw new IOException("Failed to upload from URL: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteAssets(List<AssetToDeleteDto> assets) throws Exception {
        if (assets == null || assets.isEmpty()) {
            return;
        }

        // 1. Tách danh sách dựa trên Resource Type từ DB
        List<String> imageIds = assets.stream()
                .filter(a -> a.getAssetType().equals(AssetType.IMAGE))
                .map(AssetToDeleteDto::getPublicId)
                .collect(Collectors.toList());

        List<String> rawIds = assets.stream()
                .filter(a -> a.getAssetType().equals(AssetType.DOCUMENT))
                .map(AssetToDeleteDto::getPublicId)
                .collect(Collectors.toList());

        if (!imageIds.isEmpty()) {
            batchDelete(imageIds, "image");
        }

        if (!rawIds.isEmpty()) {
            batchDelete(rawIds, "raw");
        }
    }

    // ##################################################
    // #################  UTILS METHOD  ##################
    // ##################################################
    private void batchDelete(List<String> publicIds, String resourceType) throws Exception {
        int MAX_IDS_PER_REQUEST = 100;

        Map<String, Object> params = ObjectUtils.asMap("resource_type", resourceType);

        for (int i = 0; i < publicIds.size(); i += MAX_IDS_PER_REQUEST) {
            int end = Math.min(i + MAX_IDS_PER_REQUEST, publicIds.size());
            List<String> subPublicIds = publicIds.subList(i, end);

            try {
                cloudinary.api().deleteResources(subPublicIds, params);
            } catch (Exception e) {
                log.error("Failed to delete batch {}: {}", resourceType, e.getMessage(), e);
            }
        }
    }
}
