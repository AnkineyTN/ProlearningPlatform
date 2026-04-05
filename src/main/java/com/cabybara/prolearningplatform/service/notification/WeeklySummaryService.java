package com.cabybara.prolearningplatform.service.notification;

public interface WeeklySummaryService {
    void processWeeklySummaries();

    void sendWeeklySummaryToUser(Long userId);
}
