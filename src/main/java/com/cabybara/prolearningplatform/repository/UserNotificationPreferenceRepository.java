package com.cabybara.prolearningplatform.repository;

import com.cabybara.prolearningplatform.model.noti.UserNotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserNotificationPreferenceRepository extends JpaRepository<UserNotificationPreference, Long> {

    Optional<UserNotificationPreference> findByUserId(Long userId);

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.dueCardReminderEnabled = true")
    List<Long> findUserIdsByDueCardReminderEnabled();

    @Query("""
            SELECT p.user.id FROM UserNotificationPreference p
            WHERE p.dailyTodoReminderEnabled = true
              AND p.dailyTodoReminderHour <= :currentHour
              AND (p.lastDailyTodoReminderAt IS NULL OR p.lastDailyTodoReminderAt < :today)
            """)
    List<Long> findUserIdsForDailyTodoReminderDue(@Param("currentHour") int currentHour, @Param("today") LocalDate today);

    @Query("""
            SELECT p.user.id FROM UserNotificationPreference p
            WHERE p.weeklyTodoReminderEnabled = true
              AND p.weeklyTodoReminderHour <= :currentHour
              AND (p.lastWeeklyTodoReminderAt IS NULL OR p.lastWeeklyTodoReminderAt < :today)
            """)
    List<Long> findUserIdsForWeeklyTodoReminderDue(@Param("currentHour") int currentHour, @Param("today") LocalDate today);

    @Query("""
            SELECT p.user.id FROM UserNotificationPreference p
            WHERE p.goalDeadlineReminderEnabled = true
              AND p.goalReminderHour <= :currentHour
              AND (p.lastGoalDeadlineReminderAt IS NULL OR p.lastGoalDeadlineReminderAt < :today)
            """)
    List<Long> findUserIdsForGoalDeadlineReminderDue(@Param("currentHour") int currentHour, @Param("today") LocalDate today);

    @Query("""
            SELECT p.user.id FROM UserNotificationPreference p
            WHERE p.goalInactiveReminderEnabled = true
              AND p.goalReminderHour <= :currentHour
              AND (p.lastGoalInactiveReminderAt IS NULL OR p.lastGoalInactiveReminderAt < :today)
            """)
    List<Long> findUserIdsForGoalInactiveReminderDue(@Param("currentHour") int currentHour, @Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE UserNotificationPreference p SET p.lastDailyTodoReminderAt = :today WHERE p.user.id IN :userIds")
    void markDailyTodoReminderSent(@Param("userIds") List<Long> userIds, @Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE UserNotificationPreference p SET p.lastWeeklyTodoReminderAt = :today WHERE p.user.id IN :userIds")
    void markWeeklyTodoReminderSent(@Param("userIds") List<Long> userIds, @Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE UserNotificationPreference p SET p.lastGoalDeadlineReminderAt = :today WHERE p.user.id IN :userIds")
    void markGoalDeadlineReminderSent(@Param("userIds") List<Long> userIds, @Param("today") LocalDate today);

    @Modifying
    @Query("UPDATE UserNotificationPreference p SET p.lastGoalInactiveReminderAt = :today WHERE p.user.id IN :userIds")
    void markGoalInactiveReminderSent(@Param("userIds") List<Long> userIds, @Param("today") LocalDate today);

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.dailyTodoReminderEnabled = true")
    List<Long> findAllUserIdsWithDailyTodoReminderEnabled();

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.weeklyTodoReminderEnabled = true")
    List<Long> findAllUserIdsWithWeeklyTodoReminderEnabled();

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.goalDeadlineReminderEnabled = true")
    List<Long> findAllUserIdsWithGoalDeadlineReminderEnabled();

    @Query("SELECT p.user.id FROM UserNotificationPreference p WHERE p.goalInactiveReminderEnabled = true")
    List<Long> findAllUserIdsWithGoalInactiveReminderEnabled();
}
