package com.cabybara.prolearningplatform.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserFavoriteSound;

public interface PomodoroUserFavoriteSoundRepository extends JpaRepository<PomodoroUserFavoriteSound, Long> {
    
    List<PomodoroUserFavoriteSound> findByUserId(Long userId);

    @Query("SELECT f.sound.id FROM PomodoroUserFavoriteSound f WHERE f.user.id = :userId")
    Set<Long> findFavoriteSoundIdsByUserId(@Param("userId") Long userId);

    boolean existsByUserIdAndSoundId(Long userId, Long soundId);

    @Modifying
    @Query("DELETE FROM PomodoroUserFavoriteSound f WHERE f.user.id = :userId AND f.sound.id = :soundId")
    void deleteByUserIdAndSoundId(@Param("userId") Long userId, @Param("soundId") Long soundId);
}
