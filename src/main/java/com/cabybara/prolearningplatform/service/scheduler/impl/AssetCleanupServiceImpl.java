package com.cabybara.prolearningplatform.service.scheduler.impl;

import com.cabybara.prolearningplatform.dto.helper.AssetToDeleteDto;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.scheduler.AssetCleanupService;
import com.cabybara.prolearningplatform.service.asset.AssetService;
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
    private static final String SCHEDULER_TIME_ZONE = "Asia/Ho_Chi_Minh";

    private final AssetService assetService;
    private final CloudinaryService cloudinaryService;

    @Scheduled(cron = "0 0 3 * * ?", zone = SCHEDULER_TIME_ZONE)
    @Async("heavyTaskExecutor")
    public void cleanupAssets() {
        OffsetDateTime cutoffTime = OffsetDateTime.now().minusHours(24);

        List<Asset> toCleanup = assetService.findAssetsToCleanup(cutoffTime);

        if (toCleanup.isEmpty()) {
            log.debug("Asset cleanup: nothing to clean.");
            return;
        }

        log.info("Asset cleanup: found {} assets to delete", toCleanup.size());

        List<AssetToDeleteDto> toDeleteFromCloudinary = toCleanup.stream()
                .filter(a -> a.getPublicId() != null && !a.getPublicId().isBlank())
                .filter(a -> a.getUser() != null)
                .map(a -> new AssetToDeleteDto(a.getPublicId(), a.getType()))
                .toList();

        if (!toDeleteFromCloudinary.isEmpty()) {
            try {
                cloudinaryService.deleteAssets(toDeleteFromCloudinary);
                log.info("Asset cleanup: deleted {} files from Cloudinary", toDeleteFromCloudinary.size());
            } catch (Exception e) {
                log.error("Asset cleanup: Cloudinary deletion failed, proceeding with DB cleanup", e);
            }
        }

        assetService.deleteAllAssets(toCleanup);
        log.info("Asset cleanup: removed {} records from DB", toCleanup.size());
    }
}
