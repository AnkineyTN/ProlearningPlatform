package com.cabybara.prolearningplatform.service.scheduling.impl;

import com.cabybara.prolearningplatform.dto.helper.UserDueStatDto;
import com.cabybara.prolearningplatform.repository.CardItemRepository;
import com.cabybara.prolearningplatform.repository.DeviceTokenRepository;
import com.cabybara.prolearningplatform.service.fcm.NotificationService;
import com.cabybara.prolearningplatform.service.scheduling.NotificationSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationSchedulerServiceImpl implements NotificationSchedulerService {
    private final CardItemRepository cardItemRepository;
    private final NotificationService notificationService;

    @Scheduled(cron = "0 0 8,13,20 * * ?")
    public void scanAndSendReminders() {
        log.info("Starting study reminder scan...");
        LocalDateTime now = LocalDateTime.now();

        List<UserDueStatDto> stats = cardItemRepository.findUsersWithDueCards(now);

        for (UserDueStatDto stat : stats) {
            if (stat.getDueCount() > 0) {
                notificationService.sendPersonalizedNotification(stat.getUserId(), stat.getDueCount());
            }
        }

        log.info("Scan finished. Notification tasks submitted for {} users.", stats.size());
    }


}
