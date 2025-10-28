package com.cabybara.prolearningplatform.service.scheduling.impl;

import com.cabybara.prolearningplatform.model.ImageAsset;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.scheduling.ImageCleanupService;
import com.cabybara.prolearningplatform.service.upload.ImageAssetService;
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
public class ImageCleanupServiceImpl implements ImageCleanupService {
    private final ImageAssetService imageAssetService;
    private final CloudinaryService cloudinaryService;

    @Scheduled(cron = "0 0 3 * * ?")
    @Async("heavyTaskExecutor")
    @Transactional
    public void cleanupPendingImages() {
        OffsetDateTime cutoffTime = OffsetDateTime.now().minusHours(24);

        List<ImageAsset> oldPendingAssets = imageAssetService
                .findOldPendingImages(cutoffTime);

        if (oldPendingAssets.isEmpty()) {
            log.info("Nothing to cleanup image assets are PENDING");
            return;
        }

        log.info("Found {} image PENDING. ", oldPendingAssets.size());

        List<String> publicIds = oldPendingAssets.stream()
                .map(ImageAsset::getPublicId)
                .toList();

        try {
            imageAssetService.deleteAllAssets(oldPendingAssets);

            cloudinaryService.deleteImages(publicIds);
        } catch (Exception e) {
            log.error("Error during cleanup image assets", e);
        }

        log.info("Cleaned up image assets");
    }
}
