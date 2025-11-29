package com.cabybara.prolearningplatform.service.scheduling.impl;

import com.cabybara.prolearningplatform.dto.helper.AssetToDeleteDto;
import com.cabybara.prolearningplatform.mapper.AssetMapper;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.scheduling.AssetCleanupService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssetCleanupServiceImpl implements AssetCleanupService {
    private final AssetService assetService;
    private final CloudinaryService cloudinaryService;
    private final AssetMapper assetMapper;

    @Scheduled(cron = "0 0 3 * * ?")
    @Async("heavyTaskExecutor")
    @Transactional
    public void cleanupAssets() {
        OffsetDateTime cutoffTime = OffsetDateTime.now().minusHours(24);

        List<Asset> cleanupAssets = assetService
                .findAssetsToCleanup(cutoffTime);

        if (cleanupAssets.isEmpty()) {
            log.info("Nothing to cleanup assets are PENDING");
            return;
        }

        log.info("Found {} asset to cleanup. ", cleanupAssets.size());

        List<AssetToDeleteDto> assetToDeleteDtos = cleanupAssets.stream()
                .map(assetMapper::toAssetToDeleteDto)
                .toList();

        try {
            assetService.deleteAllAssets(cleanupAssets);

            cloudinaryService.deleteAssets(assetToDeleteDtos);
        } catch (Exception e) {
            log.error("Error during cleanup image assets", e);
        }

        log.info("Cleaned up image assets");
    }
}
