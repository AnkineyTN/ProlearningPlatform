package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSpace;


public interface PomodoroSpaceRepository extends JpaRepository<PomodoroSpace, Long> {

    // Lấy tất cả SYSTEM space đang active
    List<PomodoroSpace> findBySourceAndIsActiveTrue(AssetSource source);

    // Lấy space của user
    List<PomodoroSpace> findByUserIdAndIsActiveTrue(Long userId);

    // Phân trang + search theo tên, lọc theo source
    // source = null → trả về cả SYSTEM lẫn USER của người dùng đó
     @Query("""
        SELECT s FROM PomodoroSpace s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    Page<PomodoroSpace> searchAvailableForUser(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM PomodoroSpace s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        AND s.source = :source
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    Page<PomodoroSpace> searchAvailableForUserBySource(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        @Param("source")  String source,
        Pageable pageable
    );

    // Lấy tất cả không phân trang (dùng khi load lần đầu, số lượng ít)
    @Query("""
        SELECT s FROM PomodoroSpace s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    List<PomodoroSpace> findAllAvailableForUser(@Param("userId") Long userId);

    // Chỉ lấy space user đã upload (My Spaces)
    @Query("""
        SELECT s FROM PomodoroSpace s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND s.source = 'USER'
        AND s.user.id = :userId
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY s.createdAt DESC
    """)
    Page<PomodoroSpace> searchUserSpaces(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    // Chỉ lấy space user đã favorite
    @Query("""
        SELECT s FROM PomodoroSpace s
        LEFT JOIN FETCH s.asset
        INNER JOIN PomodoroUserFavoriteSpace f ON f.space.id = s.id AND f.user.id = :userId
        WHERE s.isActive = true
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY f.createdAt DESC
    """)
    Page<PomodoroSpace> searchFavoriteSpaces(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Optional<PomodoroSpace> findByIdAndUserId(Long id, Long userId);
}