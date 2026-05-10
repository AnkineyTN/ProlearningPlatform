package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.ChapterStatus;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapChapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoadmapChapterRepository extends JpaRepository<RoadmapChapter, Long> {

    List<RoadmapChapter> findByRoadmapIdOrderByOrderIndex(Long roadmapId);

    Optional<RoadmapChapter> findByRoadmapIdAndOrderIndex(Long roadmapId, Integer orderIndex);

    long countByRoadmapId(Long roadmapId);

    long countByRoadmapIdAndStatus(Long roadmapId, ChapterStatus status);
}
