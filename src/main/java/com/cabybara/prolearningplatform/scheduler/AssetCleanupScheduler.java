package com.cabybara.prolearningplatform.scheduler;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.helper.AssetToDeleteDto;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssetCleanupScheduler {

    private final AssetService assetService;
    private final CloudinaryService cloudinaryService;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupOrphanAssets() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(24);
        List<Asset> toCleanup = assetService.findAssetsToCleanup(cutoff);

        if (toCleanup.isEmpty()) {
            log.debug("Asset cleanup: nothing to clean.");
            return;
        }

        log.info("Asset cleanup: found {} assets to delete", toCleanup.size());

        // Lọc ra những asset có publicId thực sự tồn tại trên Cloudinary
        List<AssetToDeleteDto> toDeleteFromCloudinary = toCleanup.stream()
            .filter(a -> a.getPublicId() != null && !a.getPublicId().isBlank())
            .filter(a -> a.getUser() != null)   // ← chỉ xóa Cloudinary với user asset
            .map(a -> new AssetToDeleteDto(a.getPublicId(), a.getType()))
            .collect(Collectors.toList());

        if (!toDeleteFromCloudinary.isEmpty()) {
            try {
                cloudinaryService.deleteAssets(toDeleteFromCloudinary);
                log.info("Asset cleanup: deleted {} files from Cloudinary", toDeleteFromCloudinary.size());
            } catch (Exception e) {
                // Log lỗi nhưng vẫn tiếp tục xóa DB để tránh data stale
                log.error("Asset cleanup: Cloudinary deletion failed, proceeding with DB cleanup", e);
            }
        }

        assetService.deleteAllAssets(toCleanup);
        log.info("Asset cleanup: removed {} records from DB", toCleanup.size());
    }
}