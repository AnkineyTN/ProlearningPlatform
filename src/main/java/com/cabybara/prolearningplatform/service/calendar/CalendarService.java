package com.cabybara.prolearningplatform.service.calendar;

import com.cabybara.prolearningplatform.dto.response.calendar.CalendarStatusResponse;
import com.cabybara.prolearningplatform.model.Todo;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface CalendarService {
    CalendarStatusResponse getStatus();

    String getAuthorizationUrl() throws IOException;

    void handleCalendarCallback(String code, String state, HttpServletResponse response) throws IOException, GeneralSecurityException;

    void updateSyncEnabled(boolean enabled);

    void disconnectCalendar();

    void createEvent(Todo todo);

    void updateEvent(Todo todo);

    void deleteEvent(String calendarEventId, Long userId);
}
