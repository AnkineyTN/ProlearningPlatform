package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.ImageStatus;
import com.cabybara.prolearningplatform.model.ImageAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface ImageAssetRepository extends JpaRepository<ImageAsset, Long> {
    List<ImageAsset> findByStatusAndCreatedAtBefore(ImageStatus status, OffsetDateTime cutoff);

    List<ImageAsset> findByStatus(ImageStatus status);
}
