package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.roadmap.RoadmapTopic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapTopicRepository extends JpaRepository<RoadmapTopic, Long> {

    long countByChapterId(Long chapterId);

    long countByChapterIdAndCompleted(Long chapterId, Boolean completed);

    @Query("SELECT COUNT(t) FROM RoadmapTopic t WHERE t.chapter.roadmap.id = :roadmapId")
    long countByRoadmapId(@Param("roadmapId") Long roadmapId);

    @Query("SELECT COUNT(t) FROM RoadmapTopic t WHERE t.chapter.roadmap.id = :roadmapId AND t.completed = true")
    long countCompletedByRoadmapId(@Param("roadmapId") Long roadmapId);

    @Query("SELECT t FROM RoadmapTopic t WHERE t.chapter.roadmap.id = :roadmapId ORDER BY t.chapter.orderIndex, t.orderIndex")
    List<RoadmapTopic> findAllByRoadmapIdOrdered(@Param("roadmapId") Long roadmapId);
}
