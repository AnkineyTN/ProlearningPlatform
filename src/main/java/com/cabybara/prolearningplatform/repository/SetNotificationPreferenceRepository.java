package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.noti.SetNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SetNotificationPreferenceRepository
        extends JpaRepository<SetNotificationPreference, Long> {

    Optional<SetNotificationPreference> findBySetId(Long setId);

    @Query("""
        SELECT p FROM SetNotificationPreference p
        JOIN FETCH p.set s
        JOIN FETCH s.user
        WHERE p.weeklySummaryEnabled = true
          AND p.weeklySummaryDay = :dayValue
        """)
    List<SetNotificationPreference> findAllByWeeklySummaryDay(@Param("dayValue") int dayValue);

    @Query("""
        SELECT p FROM SetNotificationPreference p
        JOIN FETCH p.set s
        JOIN FETCH s.user
        WHERE p.weeklySummaryEnabled = true
          AND s.user.id = :userId
        """)
    List<SetNotificationPreference> findEnabledByUserId(@Param("userId") Long userId);
}
