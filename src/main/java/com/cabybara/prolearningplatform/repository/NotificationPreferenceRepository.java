package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.noti.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository
        extends JpaRepository<NotificationPreference, Long> {

    Optional<NotificationPreference> findByUserId(Long userId);

    @Query("""
        SELECT np FROM NotificationPreference np
        WHERE np.weeklySummaryEnabled = true
          AND np.weeklySummaryDay = :dayValue
        """)
    List<NotificationPreference> findEnabledByWeeklySummaryDay(
            @Param("dayValue") int dayValue);

    @Query("SELECT np.user.id FROM NotificationPreference np " +
            "WHERE np.dueCardReminderEnabled = true")
    List<Long> findUserIdsWithDueCardReminderEnabled();

    boolean existsByUserId(Long userId);
}