package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSound;

public interface PomodoroSoundRepository extends JpaRepository<PomodoroSound, Long> {

    List<PomodoroSound> findBySourceAndIsActiveTrue(AssetSource source);

    List<PomodoroSound> findByUserIdAndIsActiveTrue(Long userId);

    @Query("""
        SELECT s FROM PomodoroSound s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    Page<PomodoroSound> searchAvailableForUser(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM PomodoroSound s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        AND s.source = :source
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    Page<PomodoroSound> searchAvailableForUserBySource(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        @Param("source")  String source,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM PomodoroSound s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND (s.source = 'SYSTEM' OR s.user.id = :userId)
        ORDER BY s.source ASC, s.createdAt DESC
    """)
    List<PomodoroSound> findAllAvailableForUser(@Param("userId") Long userId);

    @Query("""
        SELECT s FROM PomodoroSound s
        LEFT JOIN FETCH s.asset
        WHERE s.isActive = true
        AND s.source = 'USER'
        AND s.user.id = :userId
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY s.createdAt DESC
    """)
    Page<PomodoroSound> searchUserSounds(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    @Query("""
        SELECT s FROM PomodoroSound s
        LEFT JOIN FETCH s.asset
        INNER JOIN PomodoroUserFavoriteSound f ON f.sound.id = s.id AND f.user.id = :userId
        WHERE s.isActive = true
        AND (:keyword IS NULL OR LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
        ORDER BY f.createdAt DESC
    """)
    Page<PomodoroSound> searchFavoriteSounds(
        @Param("userId")  Long userId,
        @Param("keyword") String keyword,
        Pageable pageable
    );

    Optional<PomodoroSound> findByIdAndUserId(Long id, Long userId);

    @Query("SELECT s FROM PomodoroSound s LEFT JOIN FETCH s.asset WHERE s.source = 'SYSTEM' AND s.isActive = true ORDER BY s.createdAt DESC")
    List<PomodoroSound> findSystemSounds();
}