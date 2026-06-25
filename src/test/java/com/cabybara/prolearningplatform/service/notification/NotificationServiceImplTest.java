package com.cabybara.prolearningplatform.service.notification;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationListResponseDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationResponseDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.mapper.NotificationMapper;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.noti.Notification;
import com.cabybara.prolearningplatform.repository.NotificationRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.notification.impl.NotificationServiceImpl;
import com.cabybara.prolearningplatform.support.TestFixtures;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationMapper notificationMapper;

    @Mock
    private AuthenticationContext authenticationContext;

    @Mock
    private UserRepository userRepository;

    @Test
    void createNotificationUsesDefaultTitleWhenMissing() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        User user = TestFixtures.user(1L);
        CreateNotificationDto dto = CreateNotificationDto.builder()
                .userId(1L)
                .type(NotificationType.GENERAL)
                .message("hello")
                .data(Map.of("k", "v"))
                .actionUrl("/x")
                .build();
        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(notificationRepository.save(any(Notification.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Notification notification = service.createNotification(dto);

        verify(notificationRepository).save(captor.capture());
        Notification saved = captor.getValue();
        assertSame(user, saved.getUser());
        assertEquals(NotificationType.GENERAL.getDefaultTitle(), saved.getTitle());
        assertFalse(saved.getIsRead());
        assertFalse(saved.getPushSent());
        assertNotNull(saved.getCreatedAt());
        assertSame(saved, notification);
    }

    @Test
    void createNotificationsBuildsListWithDefaultTitles() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        User user = TestFixtures.user(2L);
        CreateNotificationDto dto = CreateNotificationDto.builder()
                .userId(2L)
                .type(NotificationType.ACCOUNT_ACTIVITY)
                .message("activity")
                .build();
        ArgumentCaptor<List<Notification>> captor = ArgumentCaptor.forClass(List.class);

        when(userRepository.getReferenceById(2L)).thenReturn(user);
        when(notificationRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));

        List<Notification> notifications = service.createNotifications(List.of(dto));

        verify(notificationRepository).saveAll(captor.capture());
        Notification saved = captor.getValue().get(0);
        assertEquals(NotificationType.ACCOUNT_ACTIVITY.getDefaultTitle(), saved.getTitle());
        assertEquals(1, notifications.size());
    }

    @Test
    void getUserNotificationsReturnsMappedPageAndUnreadCount() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        User user = TestFixtures.user(1L);
        Notification notification = TestFixtures.notification(1L, user, NotificationType.GENERAL, false);
        NotificationResponseDto dto = NotificationResponseDto.builder().id(1L).title("General").build();

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L, PageRequest.of(0, 5)))
                .thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 5), 1));
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(3L);
        when(notificationMapper.toResponseDtoList(List.of(notification))).thenReturn(List.of(dto));

        NotificationListResponseDto response = service.getUserNotifications(0, 5);

        assertEquals(3L, response.getUnreadCount());
        assertEquals(1, response.getNotifications().size());
        assertEquals(1L, response.getTotalElements());
    }

    @Test
    void getUnreadNotificationsUsesUnreadPageTotalAsUnreadCount() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        User user = TestFixtures.user(1L);
        Notification notification = TestFixtures.notification(1L, user, NotificationType.GENERAL, false);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(1L, PageRequest.of(0, 1)))
                .thenReturn(new PageImpl<>(List.of(notification), PageRequest.of(0, 1), 4));
        when(notificationMapper.toResponseDtoList(List.of(notification))).thenReturn(List.of(NotificationResponseDto.builder().id(1L).build()));

        NotificationListResponseDto response = service.getUnreadNotifications(0, 1);

        assertEquals(4L, response.getUnreadCount());
        assertEquals(4L, response.getTotalElements());
        assertEquals(1, response.getNotifications().size());
    }

    @Test
    void getUnreadCountDelegatesToRepository() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        when(authenticationContext.getCurrentUserId()).thenReturn(8L);
        when(notificationRepository.countByUserIdAndIsReadFalse(8L)).thenReturn(6L);

        assertEquals(6L, service.getUnreadCount());
    }

    @Test
    void markAsReadIsIdempotent() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        User user = TestFixtures.user(1L);
        Notification unread = TestFixtures.notification(1L, user, NotificationType.GENERAL, false);
        Notification read = TestFixtures.notification(2L, user, NotificationType.GENERAL, true);
        NotificationResponseDto mappedUnread = NotificationResponseDto.builder().id(1L).isRead(true).build();
        NotificationResponseDto mappedRead = NotificationResponseDto.builder().id(2L).isRead(true).build();

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(unread));
        when(notificationRepository.findByIdAndUserId(2L, 1L)).thenReturn(Optional.of(read));
        when(notificationRepository.save(unread)).thenReturn(unread);
        when(notificationMapper.toResponseDto(unread)).thenReturn(mappedUnread);
        when(notificationMapper.toResponseDto(read)).thenReturn(mappedRead);

        NotificationResponseDto first = service.markAsRead(1L);
        NotificationResponseDto second = service.markAsRead(2L);

        verify(notificationRepository).save(unread);
        verify(notificationRepository, never()).save(read);
        assertNotNull(unread.getReadAt());
        assertSame(mappedUnread, first);
        assertSame(mappedRead, second);
    }

    @Test
    void markMultipleAndMarkAllAsReadDelegateToRepository() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        when(authenticationContext.getCurrentUserId()).thenReturn(4L);
        when(notificationRepository.markAsReadByIds(any(), org.mockito.Mockito.eq(4L), any())).thenReturn(2);
        when(notificationRepository.markAllAsReadByUserId(org.mockito.Mockito.eq(4L), any())).thenReturn(5);

        assertEquals(2, service.markMultipleAsRead(List.of(1L, 2L)));
        assertEquals(5, service.markAllAsRead());
    }

    @Test
    void deleteNotificationLoadsByUserAndDeletes() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        User user = TestFixtures.user(1L);
        Notification notification = TestFixtures.notification(1L, user, NotificationType.GENERAL, false);

        when(authenticationContext.getCurrentUserId()).thenReturn(1L);
        when(notificationRepository.findByIdAndUserId(1L, 1L)).thenReturn(Optional.of(notification));

        service.deleteNotification(1L);

        verify(notificationRepository).delete(notification);
    }

    @Test
    void cleanupOldNotificationsDelegatesWithCutoffDate() {
        NotificationServiceImpl service = new NotificationServiceImpl(notificationRepository, notificationMapper, authenticationContext, userRepository);
        when(notificationRepository.deleteOldReadNotifications(any())).thenReturn(7);

        assertEquals(7, service.cleanupOldNotifications(30));
        verify(notificationRepository).deleteOldReadNotifications(any());
    }
}
