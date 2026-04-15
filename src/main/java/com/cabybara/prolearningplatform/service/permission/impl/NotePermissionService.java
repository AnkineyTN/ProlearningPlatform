package com.cabybara.prolearningplatform.service.permission.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.NoteMemberResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.dto.response.user.UserSearchResponse;
import com.cabybara.prolearningplatform.enums.NoteMemberStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.note.NoteMember;
import com.cabybara.prolearningplatform.repository.NoteMemberRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.NotificationRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
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
    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.token.invite-token-expiry-hours:72}")
    private int inviteTokenExpiryHours;

    /**
     * Check if a note is PUBLIC
     * PUBLIC notes allow all users to access with VIEWER role
     */
    private boolean isNotePublic(Long noteId) {
        return noteRepository.findPrivacyById(noteId)
            .map(p -> p == Privacy.PUBLIC)
            .orElse(false);
    }

    @Override
    public boolean hasAccess(Long userId, Long resourceId) {
        // PUBLIC notes: anyone can access (viewer role)
        if (isNotePublic(resourceId)) {
            return true;
        }
        
        // PRIVATE notes: only members can access
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
        // 1. Try to find user as a member
        var optionalMember = noteMemberRepository
            .findByNoteIdAndUserIdAndStatus(resourceId, userId, NoteMemberStatus.ACTIVE);
        
        if (optionalMember.isPresent()) {
            // User is a member: return their actual role
            return optionalMember.get().getRole().name();
        }
        
        // 2. User is not a member
        if (isNotePublic(resourceId)) {
            // PUBLIC note: non-member = VIEWER role
            return NoteRole.VIEWER.name();
        }
        
        // PRIVATE note: non-member = AccessDenied
        throw new AccessDeniedException(
            "User " + userId + " has no access to note " + resourceId);
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
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(inviteTokenExpiryHours);
            sendInviteNotifications(successUserIds, inviterName, noteTitle, noteId, setId, expiresAt);
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
        
        // Delete invite notification
        notificationRepository.deleteNoteInviteNotification(
            userId,
            NotificationType.NOTE_INVITE.name(),
            noteId.toString()
        );
    }

    @Transactional
    public AcceptByTokenResponse acceptByToken(String token) {
        NoteMember member = noteMemberRepository
            .findByInviteToken(token)
            .orElseThrow(() -> new EntityNotFoundException("Invalid invite token"));

        if (member.getInviteToken() == null || member.getInviteTokenExpiresAt() == null) {
            return new AcceptByTokenResponse(false, "Invite token has already been used or is invalid", null);
        }

        if (member.getInviteTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return new AcceptByTokenResponse(false, "Invite link has expired", null);
        }

        if (member.getStatus() != NoteMemberStatus.PENDING) {
            return new AcceptByTokenResponse(false, "Invite already processed", null);
        }

        member.setStatus(NoteMemberStatus.ACTIVE);
        member.setInviteToken(null); // xóa token sau khi dùng
        noteMemberRepository.save(member);

        return new AcceptByTokenResponse(true, null, member.getNote().getId());
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
        
        // Delete invite notification
        notificationRepository.deleteNoteInviteNotification(
            userId,
            NotificationType.NOTE_INVITE.name(),
            noteId.toString()
        );
    }

    public List<PendingInviteResponse> getPendingInvites(Long userId) {
        return noteMemberRepository
            .findByUserIdAndStatus(userId, NoteMemberStatus.PENDING)
            .stream()
            .map(m -> new PendingInviteResponse(
                m.getNote().getId(),
                m.getNote().getTitle(),
                m.getRole().name(),
                m.getNote().getSet().getId(),
                m.getCreatedAt()
            ))
            .toList();
    }

    public Page<NoteMemberResponse> getMembers(Long noteId, Pageable pageable) {
        return noteMemberRepository
            .findPagedByNoteId(noteId, pageable)
            .map(m -> new NoteMemberResponse(
                m.getUser().getId(),
                m.getUser().getFirstName(),
                m.getUser().getLastName(),
                m.getUser().getEmail(),
                m.getRole().name(),
                m.getStatus().name()
            ));
    }   

    public Page<NoteMemberResponse> searchMembers(
        Long noteId, String keyword, Pageable pageable) {
        return noteMemberRepository
            .searchMembers(noteId, keyword, pageable)
            .map(m -> new NoteMemberResponse(
                m.getUser().getId(),
                m.getUser().getFirstName(),
                m.getUser().getLastName(),
                m.getUser().getEmail(),
                m.getRole().name(),
                m.getStatus().name()
            ));
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
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(inviteTokenExpiryHours);

        noteMemberRepository.findByNoteIdAndUserId(noteId, targetUserId)
            .ifPresentOrElse(
                existing -> {
                    if (existing.getStatus() == NoteMemberStatus.ACTIVE) {
                        throw new IllegalStateException("User is already a member");
                    }
                    // Re-invite nếu đã decline trước đó
                    existing.setRole(role);
                    existing.setStatus(NoteMemberStatus.PENDING);
                    existing.setInviteToken(token);
                    existing.setInviteTokenExpiresAt(expiresAt);
                },
                () -> {
                    NoteMember member = new NoteMember();
                    member.setNote(noteRepository.getReferenceById(noteId));
                    member.setUser(userRepository.getReferenceById(targetUserId));
                    member.setRole(role);
                    member.setStatus(NoteMemberStatus.PENDING);
                    member.setInviteToken(token);
                    member.setInviteTokenExpiresAt(expiresAt);
                    noteMemberRepository.save(member);
                }
            );
    }

    public NoteRole getUserRoleInNote(Long noteId, Long userId) {
        return noteMemberRepository
            .findByNoteIdAndUserIdAndStatus(noteId, userId, NoteMemberStatus.ACTIVE)
            .map(NoteMember::getRole)
            .orElseThrow(() -> new AccessDeniedException(
                "User " + userId + " has no access to note " + noteId));
    }

    @Transactional
    public void updateMemberRole(Long noteId, Long targetUserId, NoteRole newRole, Long requesterId) {
        if (!isOwner(requesterId, noteId)) {
            throw new AccessDeniedException("Only owner can change member roles");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Cannot change your own role");
        }
        if (newRole == NoteRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role");
        }

        NoteMember member = noteMemberRepository
            .findByNoteIdAndUserIdAndStatus(noteId, targetUserId, NoteMemberStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        member.setRole(newRole);
    }

    private Long resolveUserId(InviteMemberRequest.InviteTarget target) {
        if (target.getUserId() != null) return target.getUserId();

        return userRepository.findByEmail(target.getEmail())
            .orElseThrow(() -> new EntityNotFoundException(
                "No user found with email: " + target.getEmail()))
            .getId();
    }

    @Async
    protected void sendInviteNotifications(
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
        data.put("expiresAt", expiresAt);

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

        userIds.forEach(userId ->
            noteMemberRepository.findByNoteIdAndUserId(noteId, userId)
                .ifPresent(member -> {
                    String acceptUrl = frontendUrl
                        + "/invites/accept?token=" + member.getInviteToken();

                    emailService.sendNoteInviteNotification(
                        member.getUser().getEmail(),
                        inviterName,
                        noteTitle,
                        member.getRole().name(),
                        acceptUrl
                    );
                })
        );
    }

    public Page<UserSearchResponse> searchUsers(String keyword, Long noteId, Pageable pageable) {
        return userRepository
            .searchByNameOrEmail(keyword, noteId, pageable)
            .map(UserSearchResponse::from);
    }
}
