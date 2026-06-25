package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.RoadmapStatus;
import com.cabybara.prolearningplatform.model.roadmap.Roadmap;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoadmapRepository extends JpaRepository<Roadmap, Long> {

    Page<Roadmap> findByUserId(Long userId, Pageable pageable);

    Page<Roadmap> findByUserIdAndStatus(Long userId, RoadmapStatus status, Pageable pageable);

    Optional<Roadmap> findByIdAndUserId(Long id, Long userId);
}
