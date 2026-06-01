package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.notification.MarkNotificationsReadRequestDto;
import com.cabybara.prolearningplatform.dto.request.user.DeviceTokenRegistrationDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationListResponseDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationResponseDto;
import com.cabybara.prolearningplatform.enums.FCMPlatform;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.mapper.NotificationMapper;
import com.cabybara.prolearningplatform.service.fcm.DeviceTokenService;
import com.cabybara.prolearningplatform.service.fcm.FCMService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
import com.cabybara.prolearningplatform.service.notification.WeeklySummaryService;
import com.cabybara.prolearningplatform.support.WebMvcTestSecurityConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = NotificationController.class)
@Import(WebMvcTestSecurityConfig.class)
@ActiveProfiles("test")
class NotificationControllerWebMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;
    @MockBean
    private DeviceTokenService deviceTokenService;
    @MockBean
    private FCMService fcmService;
    @MockBean
    private WeeklySummaryService weeklySummaryService;
    @MockBean
    private NotificationDispatcher notificationDispatcher;
    @MockBean
    private NotificationMapper notificationMapper;

    @Test
    void getUserNotificationsWithoutAuthenticationReturns401() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(notificationService);
    }

    @Test
    void getUserNotificationsReturns200() throws Exception {
        NotificationListResponseDto response = NotificationListResponseDto.builder()
                .notifications(List.of())
                .unreadCount(0L)
                .currentPage(0)
                .totalPages(0)
                .totalElements(0L)
                .build();
        when(notificationService.getUserNotifications(0, 20)).thenReturn(response);

        mockMvc.perform(get("/notifications").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.data.unreadCount").value(0));
    }

    @Test
    void getUnreadCountReturns200WithCount() throws Exception {
        when(notificationService.getUnreadCount()).thenReturn(7L);

        mockMvc.perform(get("/notifications/unread/count").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.unreadCount").value(7));
    }

    @Test
    void markAsReadReturns200() throws Exception {
        NotificationResponseDto dto = NotificationResponseDto.builder()
                .id(3L).type(NotificationType.GENERAL).isRead(true).build();
        when(notificationService.markAsRead(3L)).thenReturn(dto);

        mockMvc.perform(patch("/notifications/3/read").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification marked as read"))
                .andExpect(jsonPath("$.data.id").value(3))
                .andExpect(jsonPath("$.data.isRead").value(true));
    }

    @Test
    void markMultipleAsReadReturns200WithCount() throws Exception {
        when(notificationService.markMultipleAsRead(anyList())).thenReturn(2);
        MarkNotificationsReadRequestDto request = new MarkNotificationsReadRequestDto();
        request.setNotificationIds(List.of(1L, 2L));

        mockMvc.perform(patch("/notifications/read").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.markedCount").value(2));
    }

    @Test
    void markMultipleAsReadRejectsEmptyListWith400() throws Exception {
        MarkNotificationsReadRequestDto request = new MarkNotificationsReadRequestDto();
        request.setNotificationIds(List.of());

        mockMvc.perform(patch("/notifications/read").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));

        verifyNoInteractions(notificationService);
    }

    @Test
    void markAllAsReadReturns200() throws Exception {
        when(notificationService.markAllAsRead()).thenReturn(5);

        mockMvc.perform(patch("/notifications/read-all").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.markedCount").value(5));
    }

    @Test
    void deleteNotificationReturns200() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .delete("/notifications/4").with(jwt()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Notification deleted successfully"));

        verify(notificationService).deleteNotification(4L);
    }

    @Test
    void registerDeviceReturns200() throws Exception {
        DeviceTokenRegistrationDto request = new DeviceTokenRegistrationDto();
        request.setToken("device-token");
        request.setPlatform(FCMPlatform.WEB);

        mockMvc.perform(post("/notifications/device/register").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"));

        verify(deviceTokenService).saveOrUpdateToken(request);
    }

    @Test
    void registerDeviceRejectsMissingPlatformWith400() throws Exception {
        DeviceTokenRegistrationDto request = new DeviceTokenRegistrationDto();
        request.setToken("device-token");

        mockMvc.perform(post("/notifications/device/register").with(jwt())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"));

        verifyNoInteractions(deviceTokenService);
    }
}
