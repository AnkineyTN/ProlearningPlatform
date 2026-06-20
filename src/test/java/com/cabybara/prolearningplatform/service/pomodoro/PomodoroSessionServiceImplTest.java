package com.cabybara.prolearningplatform.service.pomodoro;

import com.cabybara.prolearningplatform.dto.request.pomodoro.LogSessionRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.WeeklyStatsResponseDto;
import com.cabybara.prolearningplatform.enums.PomodoroSessionType;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSession;
import com.cabybara.prolearningplatform.repository.PomodoroSessionRepository;
import com.cabybara.prolearningplatform.service.pomodoro.impl.PomodoroSessionServiceImpl;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PomodoroSessionServiceImplTest {

    @Mock
    private PomodoroSessionRepository sessionRepository;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private UserService userService;

    @Test
    void logSessionSavesDurationAndCycles() {
        PomodoroSessionServiceImpl service = new PomodoroSessionServiceImpl(
                sessionRepository, authenticationContext, userService);

        User user = TestFixtures.user(1L);
        OffsetDateTime now = OffsetDateTime.now();

        LogSessionRequestDto dto = new LogSessionRequestDto();
        dto.setType(PomodoroSessionType.POMODORO);
        dto.setDuration(1500);
        dto.setPlanned(1500);
        dto.setCompleted(true);
        dto.setStartedAt(now.minusMinutes(25));
        dto.setEndedAt(now);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        ArgumentCaptor<PomodoroSession> captor = ArgumentCaptor.forClass(PomodoroSession.class);
        when(sessionRepository.save(captor.capture())).thenReturn(null);

        service.logSession(dto);

        verify(sessionRepository).save(any(PomodoroSession.class));
        PomodoroSession saved = captor.getValue();
        assertEquals(1500, saved.getDuration());
        assertEquals(PomodoroSessionType.POMODORO, saved.getType());
        assertSame(user, saved.getUser());
    }

    @Test
    void logSessionDefaultsMissingFields() {
        PomodoroSessionServiceImpl service = new PomodoroSessionServiceImpl(
                sessionRepository, authenticationContext, userService);

        User user = TestFixtures.user(1L);
        OffsetDateTime now = OffsetDateTime.now();

        LogSessionRequestDto dto = new LogSessionRequestDto();
        dto.setType(PomodoroSessionType.SHORT_BREAK);
        dto.setDuration(300);
        dto.setPlanned(300);
        dto.setCompleted(false);
        dto.setStartedAt(now.minusMinutes(5));
        dto.setEndedAt(now);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(userService.getUserById(1L)).thenReturn(user);

        service.logSession(dto);

        verify(sessionRepository).save(any(PomodoroSession.class));
    }

    @Test
    void getWeeklyStatsReturnsDailyBreakdown() {
        PomodoroSessionServiceImpl service = new PomodoroSessionServiceImpl(
                sessionRepository, authenticationContext, userService);

        LocalDate monday = LocalDate.of(2026, 6, 15);
        User user = TestFixtures.user(1L);

        OffsetDateTime mon10am = monday.atTime(10, 0).atZone(ZoneId.of("UTC")).toOffsetDateTime();
        OffsetDateTime mon2pm = monday.atTime(14, 0).atZone(ZoneId.of("UTC")).toOffsetDateTime();
        OffsetDateTime tue11am = monday.plusDays(1).atTime(11, 0).atZone(ZoneId.of("UTC")).toOffsetDateTime();

        PomodoroSession s1 = PomodoroSession.builder()
                .user(user).type(PomodoroSessionType.POMODORO).duration(1500).planned(1500)
                .completed(true).startedAt(mon10am).endedAt(mon10am.plusMinutes(25)).build();
        s1.setId(1L);

        PomodoroSession s2 = PomodoroSession.builder()
                .user(user).type(PomodoroSessionType.POMODORO).duration(1200).planned(1200)
                .completed(true).startedAt(mon2pm).endedAt(mon2pm.plusMinutes(20)).build();
        s2.setId(2L);

        PomodoroSession s3 = PomodoroSession.builder()
                .user(user).type(PomodoroSessionType.POMODORO).duration(1800).planned(1800)
                .completed(false).startedAt(tue11am).endedAt(tue11am.plusMinutes(30)).build();
        s3.setId(3L);

        List<PomodoroSession> sessions = new ArrayList<>(List.of(s1, s2, s3));

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(sessionRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(sessions);
        when(sessionRepository.findRecentCompletedDays(eq(1L), anyString()))
                .thenReturn(List.of());

        WeeklyStatsResponseDto result = service.getWeeklyStats(monday, "UTC");

        assertNotNull(result);
        assertEquals(7, result.getDailyBreakdown().size());
        assertEquals(45, result.getDailyBreakdown().get(0).getFocusMinutes());
        assertEquals(2, result.getDailyBreakdown().get(0).getSessions());
        assertEquals(30, result.getDailyBreakdown().get(1).getFocusMinutes());
        assertEquals(1, result.getDailyBreakdown().get(1).getSessions());
        for (int i = 2; i < 7; i++) {
            assertEquals(0, result.getDailyBreakdown().get(i).getFocusMinutes());
        }
        assertEquals(75, result.getTotalFocusMinutes());
        assertEquals(3, result.getTotalSessions());
    }

    @Test
    void getWeeklyStatsWithEmptyWeekReturnsZeros() {
        PomodoroSessionServiceImpl service = new PomodoroSessionServiceImpl(
                sessionRepository, authenticationContext, userService);

        LocalDate monday = LocalDate.of(2026, 6, 15);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(sessionRepository.findByUserIdAndDateRange(eq(1L), any(), any()))
                .thenReturn(List.of());
        when(sessionRepository.findRecentCompletedDays(eq(1L), anyString()))
                .thenReturn(List.of());

        WeeklyStatsResponseDto result = service.getWeeklyStats(monday, "UTC");

        assertEquals(0, result.getTotalFocusMinutes());
        assertEquals(0, result.getTotalSessions());
        for (var day : result.getDailyBreakdown()) {
            assertEquals(0, day.getFocusMinutes());
        }
    }
}
