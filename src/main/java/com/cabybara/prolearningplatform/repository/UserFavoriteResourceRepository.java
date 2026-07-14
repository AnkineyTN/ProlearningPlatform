package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.model.UserFavoriteResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserFavoriteResourceRepository extends JpaRepository<UserFavoriteResource, Long> {

    Optional<UserFavoriteResource> findByUserIdAndResourceIdAndResourceType(Long userId, Long resourceId, ContentType resourceType);

    boolean existsByUserIdAndResourceIdAndResourceType(Long userId, Long resourceId, ContentType resourceType);

    Page<UserFavoriteResource> findByUserIdAndResourceType(Long userId, ContentType resourceType, Pageable pageable);

    Page<UserFavoriteResource> findByUserId(Long userId, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT ufr.resourceId FROM UserFavoriteResource ufr " +
            "WHERE ufr.userId = :userId " +
            "AND ufr.resourceType = :resourceType " +
            "AND ufr.resourceId IN :resourceIds")
    java.util.Set<Long> findFavoritedResourceIds(
            @org.springframework.data.repository.query.Param("userId") Long userId,
            @org.springframework.data.repository.query.Param("resourceIds") java.util.List<Long> resourceIds,
            @org.springframework.data.repository.query.Param("resourceType") ContentType resourceType);

    @org.springframework.data.jpa.repository.Query(value = """
        SELECT f FROM UserFavoriteResource f
        LEFT JOIN Note n ON f.resourceId = n.id AND f.resourceType = com.cabybara.prolearningplatform.enums.ContentType.NOTE
        LEFT JOIN Flashcard fc ON f.resourceId = fc.id AND f.resourceType = com.cabybara.prolearningplatform.enums.ContentType.FLASHCARD
        LEFT JOIN Exam e ON f.resourceId = e.id AND f.resourceType = com.cabybara.prolearningplatform.enums.ContentType.EXAM
        WHERE f.user.id = :userId
        AND (:resourceType IS NULL OR f.resourceType = :resourceType)
        AND (:q IS NULL OR 
             (f.resourceType = com.cabybara.prolearningplatform.enums.ContentType.NOTE AND LOWER(n.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))) OR
             (f.resourceType = com.cabybara.prolearningplatform.enums.ContentType.FLASHCARD AND LOWER(fc.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%'))) OR
             (f.resourceType = com.cabybara.prolearningplatform.enums.ContentType.EXAM AND LOWER(e.title) LIKE LOWER(CONCAT('%', CAST(:q AS string), '%')))
            )
        """)
    Page<UserFavoriteResource> searchFavoriteResources(
        @org.springframework.data.repository.query.Param("userId") Long userId,
        @org.springframework.data.repository.query.Param("resourceType") ContentType resourceType,
        @org.springframework.data.repository.query.Param("q") String q,
        Pageable pageable
    );
}
