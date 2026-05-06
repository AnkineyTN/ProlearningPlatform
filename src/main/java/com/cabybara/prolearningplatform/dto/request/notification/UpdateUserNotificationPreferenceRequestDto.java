package com.cabybara.prolearningplatform.dto.request.notification;

import lombok.Data;

@Data
public class UpdateUserNotificationPreferenceRequestDto {

    private Boolean dueCardReminderEnabled;
    private Boolean systemAnnouncementEnabled;
    private Boolean accountActivityEnabled;
}
