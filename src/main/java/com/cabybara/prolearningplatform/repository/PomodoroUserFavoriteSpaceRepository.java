package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserFavoriteSpace;

public interface PomodoroUserFavoriteSpaceRepository extends JpaRepository<PomodoroUserFavoriteSpace, Long>  {
    List<PomodoroUserFavoriteSpace> findByUserId(Long userId);

    // Trả về Set<spaceId> để check O(1)
    @Query("SELECT f.space.id FROM PomodoroUserFavoriteSpace f WHERE f.user.id = :userId")
    Set<Long> findFavoriteSpaceIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndSpaceId(Long userId, Long spaceId);

    @Modifying
    @Query("DELETE FROM PomodoroUserFavoriteSpace f WHERE f.user.id = :userId AND f.space.id = :spaceId")
    void deleteByUserIdAndSpaceId(@Param("userId") Long userId, @Param("spaceId") Long spaceId);
}
