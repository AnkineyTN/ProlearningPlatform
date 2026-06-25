package com.cabybara.prolearningplatform.service.calendar.impl;

import com.cabybara.prolearningplatform.dto.response.calendar.CalendarStatusResponse;
import com.cabybara.prolearningplatform.model.Todo;
import com.cabybara.prolearningplatform.model.UserCalendarSetting;
import com.cabybara.prolearningplatform.repository.UserCalendarSettingRepository;
import com.cabybara.prolearningplatform.service.calendar.CalendarService;
import com.cabybara.prolearningplatform.service.redis.RedisService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.MemoryDataStoreFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarServiceImpl implements CalendarService {

    private final UserCalendarSettingRepository calendarSettingRepository;
    private final AuthenticationContext authenticationContext;
    private final RedisService redisService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${google.calendar.redirect-uri}")
    private String calendarRedirectUri;

    @Value("${google.calendar.frontend-success-url}")
    private String frontendSuccessUrl;

    @Value("${google.calendar.frontend-failure-url}")
    private String frontendFailureUrl;

    private static final String APP_NAME = "Prolearning Platform";
    private static final String STATE_KEY_PREFIX = "calendar_state:";
    private static final int STATE_TTL_SECONDS = 600;
    private static final String PRIMARY_CALENDAR = "primary";

    private GoogleAuthorizationCodeFlow buildCalendarFlow() throws IOException {
        return new GoogleAuthorizationCodeFlow.Builder(
                new com.google.api.client.http.javanet.NetHttpTransport(),
                JacksonFactory.getDefaultInstance(),
                clientId,
                clientSecret,
                Collections.singleton(CalendarScopes.CALENDAR_EVENTS)
        )
        .setDataStoreFactory(new MemoryDataStoreFactory())
        .setAccessType("offline")
        .build();
    }

    private Calendar buildCalendarService(UserCalendarSetting setting) throws GeneralSecurityException, IOException {
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(setting.getRefreshToken())
                .build();

        return new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                JacksonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials)
        ).setApplicationName(APP_NAME).build();
    }

    @Override
    public CalendarStatusResponse getStatus() {
        Long userId = authenticationContext.getCurrentUserId();
        return calendarSettingRepository.findByUserId(userId)
                .map(s -> CalendarStatusResponse.builder()
                        .connected(s.getRefreshToken() != null)
                        .syncEnabled(Boolean.TRUE.equals(s.getCalendarSyncEnabled()))
                        .build())
                .orElseGet(() -> CalendarStatusResponse.builder()
                        .connected(false)
                        .syncEnabled(false)
                        .build());
    }

    @Override
    public String getAuthorizationUrl() throws IOException {
        Long userId = authenticationContext.getCurrentUserId();
        String state = UUID.randomUUID().toString();
        redisService.set(STATE_KEY_PREFIX + state, userId.toString(), STATE_TTL_SECONDS);

        return buildCalendarFlow()
                .newAuthorizationUrl()
                .setRedirectUri(calendarRedirectUri)
                .setState(state)
                .set("prompt", "consent")
                .build();
    }

    @Override
    @Transactional
    public void handleCalendarCallback(String code, String state, HttpServletResponse response) throws IOException, GeneralSecurityException {
        if (code == null || state == null) {
            response.sendRedirect(frontendFailureUrl + "?reason=access_denied");
            return;
        }

        Object rawValue = redisService.get(STATE_KEY_PREFIX + state);
        if (rawValue == null) {
            response.sendRedirect(frontendFailureUrl + "?reason=invalid_state");
            return;
        }
        redisService.delete(STATE_KEY_PREFIX + state);

        Long userId = Long.parseLong(rawValue.toString());

        TokenResponse tokenResponse;
        try {
            tokenResponse = buildCalendarFlow()
                    .newTokenRequest(code)
                    .setRedirectUri(calendarRedirectUri)
                    .execute();
        } catch (Exception e) {
            log.error("Failed to exchange calendar auth code for userId {}", userId, e);
            response.sendRedirect(frontendFailureUrl + "?reason=token_exchange_failed");
            return;
        }

        if (tokenResponse.getRefreshToken() == null) {
            log.warn("No refresh token received for userId {}. User may need to revoke and re-authorize.", userId);
        }

        UserCalendarSetting setting = calendarSettingRepository.findByUserId(userId)
                .orElse(UserCalendarSetting.builder().userId(userId).build());

        setting.setAccessToken(tokenResponse.getAccessToken());
        if (tokenResponse.getRefreshToken() != null) {
            setting.setRefreshToken(tokenResponse.getRefreshToken());
        }
        setting.setCalendarSyncEnabled(true);
        if (tokenResponse.getExpiresInSeconds() != null) {
            setting.setTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResponse.getExpiresInSeconds()));
        }

        calendarSettingRepository.save(setting);
        response.sendRedirect(frontendSuccessUrl);
    }

    @Override
    @Transactional
    public void updateSyncEnabled(boolean enabled) {
        Long userId = authenticationContext.getCurrentUserId();
        UserCalendarSetting setting = calendarSettingRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Google Calendar not connected"));
        setting.setCalendarSyncEnabled(enabled);
        calendarSettingRepository.save(setting);
    }

    @Override
    @Transactional
    public void disconnectCalendar() {
        Long userId = authenticationContext.getCurrentUserId();
        calendarSettingRepository.findByUserId(userId).ifPresent(setting -> {
            setting.setRefreshToken(null);
            setting.setAccessToken(null);
            setting.setTokenExpiresAt(null);
            setting.setCalendarSyncEnabled(false);
            calendarSettingRepository.save(setting);
        });
    }

    @Override
    public void createEvent(Todo todo) {
        if (todo.getDueDate() == null) return;

        Long userId = todo.getUser().getId();
        calendarSettingRepository.findByUserId(userId).ifPresent(setting -> {
            if (!isSyncActive(setting)) return;
            try {
                Calendar service = buildCalendarService(setting);
                Event created = service.events().insert(PRIMARY_CALENDAR, buildEvent(todo)).execute();
                todo.setCalendarEventId(created.getId());
            } catch (Exception e) {
                log.error("Failed to create Google Calendar event for todo {}", todo.getId(), e);
            }
        });
    }

    @Override
    public void updateEvent(Todo todo) {
        Long userId = todo.getUser().getId();

        // dueDate removed → delete event
        if (todo.getDueDate() == null && todo.getCalendarEventId() != null) {
            deleteEvent(todo.getCalendarEventId(), userId);
            todo.setCalendarEventId(null);
            return;
        }

        if (todo.getDueDate() == null) return;

        calendarSettingRepository.findByUserId(userId).ifPresent(setting -> {
            if (!isSyncActive(setting)) return;
            try {
                Calendar service = buildCalendarService(setting);
                if (todo.getCalendarEventId() != null) {
                    service.events().update(PRIMARY_CALENDAR, todo.getCalendarEventId(), buildEvent(todo)).execute();
                } else {
                    // dueDate was previously null, now set → create new event
                    Event created = service.events().insert(PRIMARY_CALENDAR, buildEvent(todo)).execute();
                    todo.setCalendarEventId(created.getId());
                }
            } catch (Exception e) {
                log.error("Failed to update Google Calendar event for todo {}", todo.getId(), e);
            }
        });
    }

    @Override
    public void deleteEvent(String calendarEventId, Long userId) {
        if (calendarEventId == null) return;

        calendarSettingRepository.findByUserId(userId).ifPresent(setting -> {
            if (setting.getRefreshToken() == null) return;
            try {
                Calendar service = buildCalendarService(setting);
                service.events().delete(PRIMARY_CALENDAR, calendarEventId).execute();
            } catch (Exception e) {
                log.error("Failed to delete Google Calendar event {}", calendarEventId, e);
            }
        });
    }

    private boolean isSyncActive(UserCalendarSetting setting) {
        return Boolean.TRUE.equals(setting.getCalendarSyncEnabled()) && setting.getRefreshToken() != null;
    }

    private Event buildEvent(Todo todo) {
        String summary = Boolean.TRUE.equals(todo.getCompleted())
                ? "✓ " + todo.getTitle()
                : todo.getTitle();

        Event event = new Event().setSummary(summary);

        if (todo.getDescription() != null && !todo.getDescription().isBlank()) {
            event.setDescription(todo.getDescription());
        }

        long startMillis = todo.getDueDate()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();
        long endMillis = todo.getDueDate()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        // All-day event (date only, not dateTime)
        EventDateTime start = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(true, startMillis, null));
        EventDateTime end = new EventDateTime()
                .setDate(new com.google.api.client.util.DateTime(true, endMillis, null));

        event.setStart(start).setEnd(end);

        if (todo.getPriority() != null) {
            String colorId = switch (todo.getPriority()) {
                case HIGH -> "11";   // Tomato
                case MEDIUM -> "5";  // Banana
                case LOW -> "2";     // Sage
            };
            event.setColorId(colorId);
        }

        return event;
    }
}
