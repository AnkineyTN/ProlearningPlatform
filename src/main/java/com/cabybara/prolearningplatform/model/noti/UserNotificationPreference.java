package com.cabybara.prolearningplatform.model.noti;

import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.*;

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
}
