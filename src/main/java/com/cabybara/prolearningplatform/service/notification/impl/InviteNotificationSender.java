package com.cabybara.prolearningplatform.service.notification.impl;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.repository.ExamMemberRepository;
import com.cabybara.prolearningplatform.repository.FlashcardMemberRepository;
import com.cabybara.prolearningplatform.repository.NoteMemberRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class InviteNotificationSender {

    private final NotificationDispatcher notificationDispatcher;
    private final FlashcardMemberRepository flashcardMemberRepository;
    private final NoteMemberRepository noteMemberRepository;
    private final ExamMemberRepository examMemberRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Async
    public void sendFlashcardInviteNotifications(
            List<Long> userIds,
            String inviterName,
            String flashcardTitle,
            Long flashcardId,
            Long setId,
            LocalDateTime expiresAt
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("flashcardId", flashcardId);
        data.put("setId", setId);
        data.put("expiresAt", expiresAt.toString());

        List<CreateNotificationDto> notifications = userIds.stream()
            .map(userId -> CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.FLASHCARD_INVITE)
                .title(inviterName + " invited you to collaborate")
                .message("Flashcard: " + flashcardTitle)
                .sendPush(true)
                .referenceId(flashcardId)
                .referenceType("FLASHCARD")
                .data(data)
                .build())
            .toList();

        notificationDispatcher.dispatchToMany(notifications);

        flashcardMemberRepository.findWithUserByFlashcardIdAndUserIdIn(flashcardId, userIds)
            .forEach(member -> emailService.sendFlashcardInviteNotification(
                member.getUser().getEmail(),
                inviterName,
                flashcardTitle,
                member.getRole().name(),
                frontendUrl + "/flashcard-invites/accept?token=" + member.getInviteToken()
            ));
    }

    @Async
    public void sendNoteInviteNotifications(
            List<Long> userIds,
            String inviterName,
            String noteTitle,
            Long noteId,
            Long setId,
            LocalDateTime expiresAt
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("noteId", noteId);
        data.put("setId", setId);
        data.put("expiresAt", expiresAt.toString());

        List<CreateNotificationDto> notifications = userIds.stream()
            .map(userId -> CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.NOTE_INVITE)
                .title(inviterName + " invited you to collaborate")
                .message("Note: " + noteTitle)
                .sendPush(true)
                .referenceId(noteId)
                .referenceParentId(setId)
                .referenceType("NOTE")
                .data(data)
                .build())
            .toList();

        notificationDispatcher.dispatchToMany(notifications);

        noteMemberRepository.findWithUserByNoteIdAndUserIdIn(noteId, userIds)
            .forEach(member -> emailService.sendNoteInviteNotification(
                member.getUser().getEmail(),
                inviterName,
                noteTitle,
                member.getRole().name(),
                frontendUrl + "/invites/accept?token=" + member.getInviteToken()
            ));
    }

    @Async
    public void sendExamInviteNotifications(
            List<Long> userIds,
            String inviterName,
            String examTitle,
            Long examId,
            Long setId,
            LocalDateTime expiresAt
    ) {
        Map<String, Object> data = new HashMap<>();
        data.put("examId", examId);
        data.put("setId", setId);
        data.put("expiresAt", expiresAt.toString());

        List<CreateNotificationDto> notifications = userIds.stream()
            .map(userId -> CreateNotificationDto.builder()
                .userId(userId)
                .type(NotificationType.EXAM_INVITE)
                .title(inviterName + " invited you to collaborate")
                .message("Exam: " + examTitle)
                .sendPush(true)
                .referenceId(examId)
                .referenceType("EXAM")
                .data(data)
                .build())
            .toList();

        notificationDispatcher.dispatchToMany(notifications);

        examMemberRepository.findWithUserByExamIdAndUserIdIn(examId, userIds)
            .forEach(member -> emailService.sendExamInviteNotification(
                member.getUser().getEmail(),
                inviterName,
                examTitle,
                member.getRole().name(),
                frontendUrl + "/exam-invites/accept?token=" + member.getInviteToken()
            ));
    }
}
