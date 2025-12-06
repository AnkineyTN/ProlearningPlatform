package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.response.NotificationListResponseDto;
import com.cabybara.prolearningplatform.dto.response.NotificationResponseDto;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.mapper.NotificationMapper;
import com.cabybara.prolearningplatform.model.Notification;
import com.cabybara.prolearningplatform.repository.NotificationRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.notification.NotificationService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;
    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public Notification createNotification(CreateNotificationDto createDto) {
        Notification notification = Notification.builder()
                .user(userRepository.getReferenceById(createDto.getUserId()))
                .type(createDto.getType())
                .title(createDto.getTitle() != null ? createDto.getTitle() : createDto.getType().getDefaultTitle())
                .message(createDto.getMessage())
                .data(createDto.getData())
                .actionUrl(createDto.getActionUrl())
                .isRead(false)
                .createdAt(OffsetDateTime.now())
                .pushSent(false)
                .build();

        notification = notificationRepository.save(notification);

        return notification;
    }

    @Override
    @Transactional
    public List<Notification> createNotifications(List<CreateNotificationDto> createDtos) {
        List<Notification> notifications = createDtos.stream()
                .map(dto -> Notification.builder()
                        .user(userRepository.getReferenceById(dto.getUserId()))
                        .type(dto.getType())
                        .title(dto.getTitle() != null ? dto.getTitle() : dto.getType().getDefaultTitle())
                        .message(dto.getMessage())
                        .data(dto.getData())
                        .actionUrl(dto.getActionUrl())
                        .isRead(false)
                        .createdAt(OffsetDateTime.now())
                        .pushSent(false)
                        .build())
                .toList();

        return notificationRepository.saveAll(notifications);
    }

    @Override
    public NotificationListResponseDto getUserNotifications(int page, int size) {
        Long userId = authenticationContext.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Notification> notificationPage = notificationRepository
                .findByUserIdOrderByCreatedAtDesc(userId, pageRequest);

        Long unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId);

        return NotificationListResponseDto.builder()
                .notifications(notificationMapper.toResponseDtoList(notificationPage.getContent()))
                .unreadCount(unreadCount)
                .currentPage(page)
                .totalPages(notificationPage.getTotalPages())
                .totalElements(notificationPage.getTotalElements())
                .build();
    }

    @Override
    public NotificationListResponseDto getUnreadNotifications(int page, int size) {
        Long userId = authenticationContext.getCurrentUserId();
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<Notification> notificationPage = notificationRepository
                .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId, pageRequest);

        Long unreadCount = notificationPage.getTotalElements();

        return NotificationListResponseDto.builder()
                .notifications(notificationMapper.toResponseDtoList(notificationPage.getContent()))
                .unreadCount(unreadCount)
                .currentPage(page)
                .totalPages(notificationPage.getTotalPages())
                .totalElements(notificationPage.getTotalElements())
                .build();
    }

    @Override
    public long getUnreadCount() {
        Long userId = authenticationContext.getCurrentUserId();
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public NotificationResponseDto markAsRead(Long notificationId) {
        Long userId = authenticationContext.getCurrentUserId();

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getIsRead()) {
            notification.setIsRead(true);
            notification.setReadAt(OffsetDateTime.now());
            notification = notificationRepository.save(notification);
        }

        return notificationMapper.toResponseDto(notification);
    }

    @Override
    @Transactional
    public int markMultipleAsRead(List<Long> notificationIds) {
        Long userId = authenticationContext.getCurrentUserId();
        return notificationRepository.markAsReadByIds(notificationIds, userId, OffsetDateTime.now());
    }

    @Override
    @Transactional
    public int markAllAsRead() {
        Long userId = authenticationContext.getCurrentUserId();
        return notificationRepository.markAllAsReadByUserId(userId, OffsetDateTime.now());
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        Long userId = authenticationContext.getCurrentUserId();

        Notification notification = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        notificationRepository.delete(notification);
    }

    @Override
    @Transactional
    public int cleanupOldNotifications(int daysOld) {
        OffsetDateTime cutoffDate = OffsetDateTime.now().minusDays(daysOld);
        return notificationRepository.deleteOldReadNotifications(cutoffDate);
    }
}

