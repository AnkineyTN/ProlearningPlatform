package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.knowledge.KnowledgeAnalysis;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface KnowledgeAnalysisRepository extends JpaRepository<KnowledgeAnalysis, Long> {

    Optional<KnowledgeAnalysis> findBySessionRefIdAndSourceType(Long sessionRefId, String sourceType);

    List<KnowledgeAnalysis> findAllBySourceTypeAndSourceIdOrderByCreatedAtDesc(String sourceType, Long sourceId);

    @Query("""
        SELECT ka FROM KnowledgeAnalysis ka
        WHERE ka.sourceType = :sourceType AND ka.sourceId = :sourceId
        ORDER BY ka.createdAt DESC
        LIMIT 1
        """)
    Optional<KnowledgeAnalysis> findLatestBySourceTypeAndSourceId(
            @Param("sourceType") String sourceType,
            @Param("sourceId") Long sourceId);

    List<KnowledgeAnalysis> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
