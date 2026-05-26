package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.noti.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {

    Optional<UserNotificationPreference> findByUserId(Long userId);

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.dueCardReminderEnabled = true")
    List<Long> findUserIdsByDueCardReminderEnabled();

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.dailyTodoReminderEnabled = true AND p.dailyTodoReminderHour = :hour")
    List<Long> findUserIdsForDailyTodoReminder(@Param("hour") int hour);

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.weeklyTodoReminderEnabled = true AND p.weeklyTodoReminderHour = :hour")
    List<Long> findUserIdsForWeeklyTodoReminder(@Param("hour") int hour);

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.goalDeadlineReminderEnabled = true AND p.goalReminderHour = :hour")
    List<Long> findUserIdsForGoalDeadlineReminder(@Param("hour") int hour);

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.goalInactiveReminderEnabled = true AND p.goalReminderHour = :hour")
    List<Long> findUserIdsForGoalInactiveReminder(@Param("hour") int hour);
}
