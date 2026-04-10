package com.cabybara.prolearningplatform.service.permission.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.enums.NoteMemberStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.model.note.NoteMember;
import com.cabybara.prolearningplatform.repository.NoteMemberRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.service.permission.ResourcePermissionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("notePermissionService")
@RequiredArgsConstructor
public class NotePermissionService implements ResourcePermissionService  {
    private final NoteMemberRepository noteMemberRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final NotificationDispatcher notificationDispatcher;

    @Override
    public boolean hasAccess(Long userId, Long resourceId) {
        return noteMemberRepository.existsByNoteIdAndUserIdAndStatus(
            resourceId, userId, NoteMemberStatus.ACTIVE);
    }

    @Override
    public boolean canEdit(Long userId, Long resourceId) {
        return noteMemberRepository
            .findByNoteIdAndUserIdAndStatus(resourceId, userId, NoteMemberStatus.ACTIVE)
            .map(m -> m.getRole() == NoteRole.OWNER || m.getRole() == NoteRole.EDITOR)
            .orElse(false);
    }

    @Override
    public boolean isOwner(Long userId, Long resourceId) {
        return noteMemberRepository
            .findByNoteIdAndUserIdAndStatus(resourceId, userId, NoteMemberStatus.ACTIVE)
            .map(m -> m.getRole() == NoteRole.OWNER)
            .orElse(false);
    }

    @Override
    public String getRole(Long userId, Long resourceId) {
        return noteMemberRepository
            .findByNoteIdAndUserIdAndStatus(resourceId, userId, NoteMemberStatus.ACTIVE)
            .map(m -> m.getRole().name())
            .orElseThrow(() -> new AccessDeniedException(
                "User " + userId + " has no access to note " + resourceId));
    }

    @Transactional
    public void addOwner(Long noteId, Long userId) {
        NoteMember owner = new NoteMember();
        owner.setNote(noteRepository.getReferenceById(noteId));
        owner.setUser(userRepository.getReferenceById(userId));
        owner.setRole(NoteRole.OWNER);
        owner.setStatus(NoteMemberStatus.ACTIVE);
        noteMemberRepository.save(owner);
    }

    @Transactional
    public List<InviteResultResponse> inviteMembers(
            Long setId,
            Long noteId,
            List<InviteMemberRequest.InviteTarget> targets,
            NoteRole role,
            Long requesterId
    ) {
        if (!isOwner(requesterId, noteId)) {
            throw new AccessDeniedException("Only owner can invite members");
        }

        String noteTitle = noteRepository.findTitleById(noteId);
        String inviterName = userRepository.findById(requesterId)
            .map(u -> u.getFirstName() + " " + u.getLastName())
            .orElse("Someone");

        List<InviteResultResponse> results = new ArrayList<>();
        List<Long> successUserIds = new ArrayList<>();

        for (InviteMemberRequest.InviteTarget target : targets) {
            try {
                Long targetUserId = resolveUserId(target);

                if (targetUserId.equals(requesterId)) {
                    results.add(InviteResultResponse.failed(target, "Cannot invite yourself"));
                    continue;
                }

                doInvite(noteId, targetUserId, role);
                results.add(InviteResultResponse.success(target, targetUserId));
                successUserIds.add(targetUserId);

            } catch (EntityNotFoundException e) {
                results.add(InviteResultResponse.failed(target, "User not found"));
            } catch (IllegalStateException e) {
                results.add(InviteResultResponse.failed(target, e.getMessage()));
            } catch (Exception e) {
                log.error("[invite] Unexpected error for target {}: {}", target, e.getMessage());
                results.add(InviteResultResponse.failed(target, "Unexpected error"));
            }
        }

        // Gửi notification cho tất cả user được invite thành công
        if (!successUserIds.isEmpty()) {
            sendInviteNotifications(successUserIds, inviterName, noteTitle, noteId, setId);
        }

        return results;
    }

    @Transactional
    public void acceptInvite(Long noteId, Long userId) {
        NoteMember member = noteMemberRepository
            .findByNoteIdAndUserId(noteId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (member.getStatus() != NoteMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invite for this note");
        }

        member.setStatus(NoteMemberStatus.ACTIVE);
    }

    @Transactional
    public void declineInvite(Long noteId, Long userId) {
        NoteMember member = noteMemberRepository
            .findByNoteIdAndUserId(noteId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (member.getStatus() != NoteMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invite for this note");
        }

        member.setStatus(NoteMemberStatus.DECLINED);
    }

    public List<PendingInviteResponse> getPendingInvites(Long userId) {
        return noteMemberRepository
            .findByUserIdAndStatus(userId, NoteMemberStatus.PENDING)
            .stream()
            .map(m -> new PendingInviteResponse(
                m.getNote().getId(),
                m.getNote().getTitle(),
                m.getRole().name(),
                m.getCreatedAt()
            ))
            .toList();
    }

    @Transactional
    public void removeMember(Long noteId, Long targetUserId, Long requesterId) {
        if (!isOwner(requesterId, noteId)) {
            throw new AccessDeniedException("Only owner can remove members");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Owner cannot remove themselves");
        }
        noteMemberRepository.deleteByNoteIdAndUserId(noteId, targetUserId);
    }

     private void doInvite(Long noteId, Long targetUserId, NoteRole role) {
        noteMemberRepository.findByNoteIdAndUserId(noteId, targetUserId)
            .ifPresentOrElse(
                existing -> {
                    if (existing.getStatus() == NoteMemberStatus.ACTIVE) {
                        throw new IllegalStateException("User is already a member");
                    }
                    // Re-invite nếu đã decline trước đó
                    existing.setRole(role);
                    existing.setStatus(NoteMemberStatus.PENDING);
                },
                () -> {
                    NoteMember member = new NoteMember();
                    member.setNote(noteRepository.getReferenceById(noteId));
                    member.setUser(userRepository.getReferenceById(targetUserId));
                    member.setRole(role);
                    member.setStatus(NoteMemberStatus.PENDING);
                    noteMemberRepository.save(member);
                }
            );
    }

    private Long resolveUserId(InviteMemberRequest.InviteTarget target) {
        if (target.getUserId() != null) return target.getUserId();

        return userRepository.findByEmail(target.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(
                "No user found with email: " + target.getEmail()))
            .getId();
    }

    private void sendInviteNotifications(
            List<Long> userIds,
            String inviterName,
            String noteTitle,
            Long noteId,
            Long setId
    ) {
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
                .build())
            .toList();

        notificationDispatcher.dispatchToMany(notifications);
    }
}
