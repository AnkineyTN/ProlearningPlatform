package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.model.noti.Notification;

import java.util.List;

public interface NotificationDispatcher {

    Notification dispatch(CreateNotificationDto createDto);

    // Dispatch notifications to multiple users
    List<Notification> dispatchToMany(List<CreateNotificationDto> createDtos);

    Notification dispatchToUser(Long userId, String title, String message,
                                 com.cabybara.prolearningplatform.enums.NotificationType type);
}

