package com.cabybara.prolearningplatform.model.noti;

import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.OffsetDateTime;

@Entity
@Table(name = "notification_preference")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "user")
@EqualsAndHashCode(exclude = "user")
public class NotificationPreference extends AbstractEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", nullable = false, unique = true)
    private User user;

    @Builder.Default
    @Column(name = "due_card_reminder_enabled", nullable = false)
    private boolean dueCardReminderEnabled = true;

    @Builder.Default
    @Column(name = "weekly_summary_enabled", nullable = false)
    private boolean weeklySummaryEnabled = true;

    @Builder.Default
    @Column(name = "weekly_summary_day", nullable = false)
    private int weeklySummaryDay = DayOfWeek.SUNDAY.getValue();

    @Column(name = "last_summary_sent_at")
    private OffsetDateTime lastSummarySentAt;

    public DayOfWeek getWeeklySummaryDayOfWeek() {
        return DayOfWeek.of(weeklySummaryDay);
    }

    public void setWeeklySummaryDayOfWeek(DayOfWeek day) {
        this.weeklySummaryDay = day.getValue();
    }
}