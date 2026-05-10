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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.internal.CreateNotificationDto;
import com.cabybara.prolearningplatform.dto.request.share.InviteMemberRequest;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.InviteResultResponse;
import com.cabybara.prolearningplatform.dto.response.share.NoteMemberResponse;
import com.cabybara.prolearningplatform.dto.response.user.UserSearchResponse;
import com.cabybara.prolearningplatform.enums.FlashcardMemberStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.flashcard.FlashcardMember;
import com.cabybara.prolearningplatform.repository.FlashcardMemberRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NotificationRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;
import com.cabybara.prolearningplatform.model.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("flashcardPermissionService")
@RequiredArgsConstructor
public class FlashcardPermissionService {
    private final FlashcardMemberRepository flashcardMemberRepository;
    private final FlashcardRepository flashcardRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.token.invite-token-expiry-hours:72}")
    private int inviteTokenExpiryHours;

    /**
     * Check if a flashcard is PUBLIC
     * PUBLIC flashcards allow all users to access with VIEWER role
     */
    private boolean isFlashcardPublic(Long flashcardId) {
        return flashcardRepository.findPrivacyById(flashcardId)
            .map(p -> p == Privacy.PUBLIC)
            .orElse(false);
    }

    public boolean hasAccess(Long userId, Long resourceId) {
        // PUBLIC flashcards: anyone can access (viewer role)
        if (isFlashcardPublic(resourceId)) {
            return true;
        }
        
        // PRIVATE flashcards: only members can access
        return flashcardMemberRepository.existsByFlashcardIdAndUserIdAndStatus(
            resourceId, userId, FlashcardMemberStatus.ACTIVE);
    }

    public boolean canEdit(Long userId, Long resourceId) {
        // Both PUBLIC and PRIVATE: user must be a MEMBER with OWNER or EDITOR role
        return flashcardMemberRepository
            .findByFlashcardIdAndUserIdAndStatus(resourceId, userId, FlashcardMemberStatus.ACTIVE)
            .map(m -> m.getRole() == NoteRole.OWNER || m.getRole() == NoteRole.EDITOR)
            .orElse(false);
    }

    public boolean isOwner(Long userId, Long resourceId) {
        return flashcardMemberRepository
            .findByFlashcardIdAndUserIdAndStatus(resourceId, userId, FlashcardMemberStatus.ACTIVE)
            .map(m -> m.getRole() == NoteRole.OWNER)
            .orElse(false);
    }

    public String getRole(Long userId, Long resourceId) {
        // 1. Try to find user as a member
        var optionalMember = flashcardMemberRepository
            .findByFlashcardIdAndUserIdAndStatus(resourceId, userId, FlashcardMemberStatus.ACTIVE);
        
        if (optionalMember.isPresent()) {
            // User is a member: return their actual role
            return optionalMember.get().getRole().name();
        }
        
        // 2. User is not a member
        if (isFlashcardPublic(resourceId)) {
            // PUBLIC flashcard: non-member = VIEWER role
            return NoteRole.VIEWER.name();
        }
        
        // PRIVATE flashcard: non-member = AccessDenied
        throw new AccessDeniedException(
            "User " + userId + " has no access to flashcard " + resourceId);
    }

    @Transactional
    public void addOwner(Long flashcardId, Long userId) {
        FlashcardMember owner = new FlashcardMember();
        owner.setFlashcard(flashcardRepository.getReferenceById(flashcardId));
        owner.setUser(userRepository.getReferenceById(userId));
        owner.setRole(NoteRole.OWNER);
        owner.setStatus(FlashcardMemberStatus.ACTIVE);
        flashcardMemberRepository.save(owner);
    }

    @Transactional
    public List<InviteResultResponse> inviteMembers(
            Long setId,
            Long flashcardId,
            List<InviteMemberRequest.InviteTarget> targets,
            NoteRole role,
            Long requesterId
    ) {
        if (!isOwner(requesterId, flashcardId)) {
            throw new AccessDeniedException("Only owner can invite members");
        }

        String flashcardTitle = flashcardRepository.findById(flashcardId)
            .map(f -> f.getTitle())
            .orElse("Unknown");
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

                doInvite(flashcardId, targetUserId, role);
                results.add(InviteResultResponse.success(target, targetUserId));
                successUserIds.add(targetUserId);

            } catch (EntityNotFoundException e) {
                results.add(InviteResultResponse.failed(target, "User not found"));
            } catch (IllegalStateException e) {
                results.add(InviteResultResponse.failed(target, e.getMessage()));
            } catch (Exception e) {
                log.error("[flashcard-invite] Unexpected error for target {}: {}", target, e.getMessage());
                results.add(InviteResultResponse.failed(target, "Unexpected error"));
            }
        }

        // Send notifications for successful invites
        if (!successUserIds.isEmpty()) {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(inviteTokenExpiryHours);
            sendInviteNotifications(successUserIds, inviterName, flashcardTitle, flashcardId, setId, expiresAt);
        }

        return results;
    }

    @Transactional
    public void acceptInvite(Long flashcardId, Long userId) {
        FlashcardMember member = flashcardMemberRepository
            .findByFlashcardIdAndUserId(flashcardId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (member.getStatus() != FlashcardMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invite for this flashcard");
        }

        member.setStatus(FlashcardMemberStatus.ACTIVE);
        
        // Delete invite notification
        notificationRepository.deleteFlashcardInviteNotification(
            userId,
            NotificationType.FLASHCARD_INVITE.name(),
            flashcardId.toString()
        );
    }

    @Transactional
    public AcceptByTokenResponse acceptByToken(String token) {
        FlashcardMember member = flashcardMemberRepository
            .findByInviteToken(token)
            .orElseThrow(() -> new EntityNotFoundException("Invalid invite token"));

        if (member.getInviteToken() == null || member.getInviteTokenExpiresAt() == null) {
            return new AcceptByTokenResponse(false, "Invite token has already been used or is invalid", null);
        }

        if (member.getInviteTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return new AcceptByTokenResponse(false, "Invite link has expired", null);
        }

        if (member.getStatus() != FlashcardMemberStatus.PENDING) {
            return new AcceptByTokenResponse(false, "Invite already processed", null);
        }

        member.setStatus(FlashcardMemberStatus.ACTIVE);
        member.setInviteToken(null);
        flashcardMemberRepository.save(member);

        return new AcceptByTokenResponse(true, null, member.getFlashcard().getId());
    }

    @Transactional
    public void declineInvite(Long flashcardId, Long userId) {
        FlashcardMember member = flashcardMemberRepository
            .findByFlashcardIdAndUserId(flashcardId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (member.getStatus() != FlashcardMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invite for this flashcard");
        }

        member.setStatus(FlashcardMemberStatus.DECLINED);
        
        // Delete invite notification
        notificationRepository.deleteFlashcardInviteNotification(
            userId,
            NotificationType.FLASHCARD_INVITE.name(),
            flashcardId.toString()
        );
    }

    public List<InviteResultResponse.PendingInviteFlashcardResponse> getPendingInvites(Long userId) {
        return flashcardMemberRepository
            .findByUserIdAndStatus(userId, FlashcardMemberStatus.PENDING)
            .stream()
            .map(m -> new InviteResultResponse.PendingInviteFlashcardResponse(
                m.getFlashcard().getId(),
                m.getFlashcard().getTitle(),
                m.getRole().name(),
                m.getFlashcard().getSet().getId(),
                m.getCreatedAt()
            ))
            .toList();
    }

    public Page<NoteMemberResponse> getMembers(Long flashcardId, Pageable pageable) {
        return flashcardMemberRepository
            .findPagedByFlashcardId(flashcardId, pageable)
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
        Long flashcardId, String keyword, Pageable pageable) {
        return flashcardMemberRepository
            .searchMembers(flashcardId, keyword, pageable)
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
    public void removeMember(Long flashcardId, Long targetUserId, Long requesterId) {
        if (!isOwner(requesterId, flashcardId)) {
            throw new AccessDeniedException("Only owner can remove members");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Owner cannot remove themselves");
        }
        flashcardMemberRepository.deleteByFlashcardIdAndUserId(flashcardId, targetUserId);
    }

    @Transactional
    public void updateMemberRole(Long flashcardId, Long targetUserId, NoteRole newRole, Long requesterId) {
        if (!isOwner(requesterId, flashcardId)) {
            throw new AccessDeniedException("Only owner can change member roles");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Cannot change your own role");
        }
        if (newRole == NoteRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role");
        }

        FlashcardMember member = flashcardMemberRepository
            .findByFlashcardIdAndUserIdAndStatus(flashcardId, targetUserId, FlashcardMemberStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        member.setRole(newRole);
    }

    private void doInvite(Long flashcardId, Long targetUserId, NoteRole role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(inviteTokenExpiryHours);

        flashcardMemberRepository.findByFlashcardIdAndUserId(flashcardId, targetUserId)
            .ifPresentOrElse(
                existing -> {
                    if (existing.getStatus() == FlashcardMemberStatus.ACTIVE) {
                        throw new IllegalStateException("User is already a member");
                    }
                    
                    // If DECLINED: delete old record and create new
                    if (existing.getStatus() == FlashcardMemberStatus.DECLINED) {
                        log.info("[flashcard-invite] Re-inviting user {} with DECLINED status, creating new invite", targetUserId);
                        flashcardMemberRepository.delete(existing);
                        createNewInvite(flashcardId, targetUserId, role, token, expiresAt);
                    } else {
                        // PENDING: update existing record
                        existing.setRole(role);
                        existing.setStatus(FlashcardMemberStatus.PENDING);
                        existing.setInviteToken(token);
                        existing.setInviteTokenExpiresAt(expiresAt);
                    }
                },
                () -> createNewInvite(flashcardId, targetUserId, role, token, expiresAt)
            );
    }

    private void createNewInvite(Long flashcardId, Long targetUserId, NoteRole role, String token, LocalDateTime expiresAt) {
        FlashcardMember member = new FlashcardMember();
        member.setFlashcard(flashcardRepository.getReferenceById(flashcardId));
        member.setUser(userRepository.getReferenceById(targetUserId));
        member.setRole(role);
        member.setStatus(FlashcardMemberStatus.PENDING);
        member.setInviteToken(token);
        member.setInviteTokenExpiresAt(expiresAt);
        flashcardMemberRepository.save(member);
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

        userIds.forEach(userId ->
            flashcardMemberRepository.findByFlashcardIdAndUserId(flashcardId, userId)
                .ifPresent(member -> {
                    String acceptUrl = frontendUrl
                        + "/flashcard-invites/accept?token=" + member.getInviteToken();

                    emailService.sendFlashcardInviteNotification(
                        member.getUser().getEmail(),
                        inviterName,
                        flashcardTitle,
                        member.getRole().name(),
                        acceptUrl
                    );
                })
        );
    }

    public Page<UserSearchResponse> searchUsers(String keyword, Long flashcardId, Pageable pageable) {
        Page<User> users;
        if (keyword == null || keyword.trim().isEmpty()) {
            users = userRepository.findAllExcludingFlashcardMembers(flashcardId, pageable);
        } else {
            users = userRepository.searchByNameOrEmailForFlashcard(keyword, flashcardId, pageable);
        }
        return users.map(UserSearchResponse::from);
    }

    public NoteRole getUserRoleInFlashcard(Long flashcardId, Long userId) {
        // 1. Try to find user as a member
        var optionalMember = flashcardMemberRepository
            .findByFlashcardIdAndUserIdAndStatus(flashcardId, userId, FlashcardMemberStatus.ACTIVE);
        
        if (optionalMember.isPresent()) {
            // User is a member: return their actual role
            return optionalMember.get().getRole();
        }
        
        // 2. User is not a member
        if (isFlashcardPublic(flashcardId)) {
            // PUBLIC flashcard: non-member = VIEWER role
            return NoteRole.VIEWER;
        }
        
        // PRIVATE flashcard: non-member = AccessDenied
        throw new AccessDeniedException(
            "User " + userId + " has no access to flashcard " + flashcardId);
    }

    /**
     * Scheduled cleanup task: Delete old PENDING and DECLINED records (older than 7 days)
     * Runs daily at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void cleanupOldInviteRecords() {
        try {
            LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
            flashcardMemberRepository.deleteOldPendingOrDeclinedRecords(cutoffDate);
            log.info("[flashcard-invite-cleanup] Successfully cleaned up old PENDING/DECLINED records older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("[flashcard-invite-cleanup] Error cleaning up old invite records: {}", e.getMessage(), e);
        }
    }
}
