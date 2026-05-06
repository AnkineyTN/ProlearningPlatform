package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.AssetStatus;
import com.cabybara.prolearningplatform.model.Asset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface AssetRepository extends JpaRepository<Asset, Long> {
    List<Asset> findByStatusAndCreatedAtBefore(AssetStatus status, OffsetDateTime cutoff);

    Asset findByUrl(String url);
}
