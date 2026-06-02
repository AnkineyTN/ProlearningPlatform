package com.cabybara.prolearningplatform.model.noti;

import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "notification_preference")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationPreference extends AbstractEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "due_card_reminder_enabled", nullable = false)
    private boolean dueCardReminderEnabled = true;

    @Builder.Default
    @Column(name = "system_announcement_enabled", nullable = false)
    private boolean systemAnnouncementEnabled = true;

    @Builder.Default
    @Column(name = "account_activity_enabled", nullable = false)
    private boolean accountActivityEnabled = true;

    @Builder.Default
    @Column(name = "daily_todo_reminder_enabled", nullable = false)
    private boolean dailyTodoReminderEnabled = true;

    @Builder.Default
    @Column(name = "daily_todo_reminder_hour", nullable = false)
    private int dailyTodoReminderHour = 20;

    @Builder.Default
    @Column(name = "weekly_todo_reminder_enabled", nullable = false)
    private boolean weeklyTodoReminderEnabled = true;

    @Builder.Default
    @Column(name = "weekly_todo_reminder_hour", nullable = false)
    private int weeklyTodoReminderHour = 20;

    @Builder.Default
    @Column(name = "goal_deadline_reminder_enabled", nullable = false)
    private boolean goalDeadlineReminderEnabled = true;

    @Builder.Default
    @Column(name = "goal_inactive_reminder_enabled", nullable = false)
    private boolean goalInactiveReminderEnabled = true;

    @Builder.Default
    @Column(name = "goal_reminder_hour", nullable = false)
    private int goalReminderHour = 9;

    @Column(name = "last_daily_todo_reminder_at")
    private LocalDate lastDailyTodoReminderAt;

    @Column(name = "last_weekly_todo_reminder_at")
    private LocalDate lastWeeklyTodoReminderAt;

    @Column(name = "last_goal_deadline_reminder_at")
    private LocalDate lastGoalDeadlineReminderAt;

    @Column(name = "last_goal_inactive_reminder_at")
    private LocalDate lastGoalInactiveReminderAt;
}
