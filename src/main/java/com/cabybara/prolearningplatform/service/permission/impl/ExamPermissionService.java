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
import com.cabybara.prolearningplatform.dto.response.user.UserSearchResponse;
import com.cabybara.prolearningplatform.enums.ExamMemberStatus;
import com.cabybara.prolearningplatform.enums.NoteRole;
import com.cabybara.prolearningplatform.enums.NotificationType;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.exam.ExamMember;
import com.cabybara.prolearningplatform.repository.ExamMemberRepository;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.NotificationRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.email.EmailService;
import com.cabybara.prolearningplatform.service.notification.NotificationDispatcher;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service("examPermissionService")
@RequiredArgsConstructor
public class ExamPermissionService {
    private final ExamMemberRepository examMemberRepository;
    private final ExamRepository examRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationDispatcher notificationDispatcher;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Value("${app.token.invite-token-expiry-hours:72}")
    private int inviteTokenExpiryHours;

    /**
     * Check if an exam is PUBLIC
     * PUBLIC exams allow all users to access with VIEWER role
     */
    private boolean isExamPublic(Long examId) {
        return examRepository.findPrivacyById(examId)
            .map(p -> p == Privacy.PUBLIC)
            .orElse(false);
    }

    public boolean hasAccess(Long userId, Long resourceId) {
        // PUBLIC exams: anyone can access (viewer role)
        if (isExamPublic(resourceId)) {
            return true;
        }
        
        // PRIVATE exams: only members can access
        return examMemberRepository.existsByExamIdAndUserIdAndStatus(
            resourceId, userId, ExamMemberStatus.ACTIVE);
    }

    public boolean canEdit(Long userId, Long resourceId) {
        // Both PUBLIC and PRIVATE: user must be a MEMBER with OWNER or EDITOR role
        return examMemberRepository
            .findByExamIdAndUserIdAndStatus(resourceId, userId, ExamMemberStatus.ACTIVE)
            .map(m -> m.getRole() == NoteRole.OWNER || m.getRole() == NoteRole.EDITOR)
            .orElse(false);
    }

    public boolean isOwner(Long userId, Long resourceId) {
        return examMemberRepository
            .findByExamIdAndUserIdAndStatus(resourceId, userId, ExamMemberStatus.ACTIVE)
            .map(m -> m.getRole() == NoteRole.OWNER)
            .orElse(false);
    }

    public String getRole(Long userId, Long resourceId) {
        // 1. Try to find user as a member
        var optionalMember = examMemberRepository
            .findByExamIdAndUserIdAndStatus(resourceId, userId, ExamMemberStatus.ACTIVE);
        
        if (optionalMember.isPresent()) {
            // User is a member: return their actual role
            return optionalMember.get().getRole().name();
        }
        
        // 2. User is not a member
        if (isExamPublic(resourceId)) {
            // PUBLIC exam: non-member = VIEWER role
            return NoteRole.VIEWER.name();
        }
        
        // PRIVATE exam: non-member = AccessDenied
        throw new AccessDeniedException(
            "User " + userId + " has no access to exam " + resourceId);
    }

    @Transactional
    public void addOwner(Long examId, Long userId) {
        ExamMember owner = new ExamMember();
        owner.setExam(examRepository.getReferenceById(examId));
        owner.setUser(userRepository.getReferenceById(userId));
        owner.setRole(NoteRole.OWNER);
        owner.setStatus(ExamMemberStatus.ACTIVE);
        examMemberRepository.save(owner);
    }

    @Transactional
    public List<InviteResultResponse> inviteMembers(
            Long setId,
            Long examId,
            List<InviteMemberRequest.InviteTarget> targets,
            NoteRole role,
            Long requesterId
    ) {
        if (!isOwner(requesterId, examId)) {
            throw new AccessDeniedException("Only owner can invite members");
        }

        String examTitle = examRepository.findById(examId)
            .map(e -> e.getTitle())
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

                doInvite(examId, targetUserId, role);
                results.add(InviteResultResponse.success(target, targetUserId));
                successUserIds.add(targetUserId);

            } catch (EntityNotFoundException e) {
                results.add(InviteResultResponse.failed(target, "User not found"));
            } catch (IllegalStateException e) {
                results.add(InviteResultResponse.failed(target, e.getMessage()));
            } catch (Exception e) {
                log.error("[exam-invite] Unexpected error for target {}: {}", target, e.getMessage());
                results.add(InviteResultResponse.failed(target, "Unexpected error"));
            }
        }

        // Send notifications for successful invites
        if (!successUserIds.isEmpty()) {
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(inviteTokenExpiryHours);
            sendInviteNotifications(successUserIds, inviterName, examTitle, examId, setId, expiresAt);
        }

        return results;
    }

    @Transactional
    public void acceptInvite(Long examId, Long userId) {
        ExamMember member = examMemberRepository
            .findByExamIdAndUserId(examId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (member.getStatus() != ExamMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invite for this exam");
        }

        member.setStatus(ExamMemberStatus.ACTIVE);
        
        // Delete invite notification
        notificationRepository.deleteExamInviteNotification(
            userId,
            NotificationType.EXAM_INVITE.name(),
            examId.toString()
        );
    }

    @Transactional
    public AcceptByTokenResponse acceptByToken(String token) {
        ExamMember member = examMemberRepository
            .findByInviteToken(token)
            .orElseThrow(() -> new EntityNotFoundException("Invalid invite token"));

        if (member.getInviteToken() == null || member.getInviteTokenExpiresAt() == null) {
            return new AcceptByTokenResponse(false, "Invite token has already been used or is invalid", null);
        }

        if (member.getInviteTokenExpiresAt().isBefore(LocalDateTime.now())) {
            return new AcceptByTokenResponse(false, "Invite link has expired", null);
        }

        if (member.getStatus() != ExamMemberStatus.PENDING) {
            return new AcceptByTokenResponse(false, "Invite already processed", null);
        }

        member.setStatus(ExamMemberStatus.ACTIVE);
        member.setInviteToken(null);
        examMemberRepository.save(member);

        return new AcceptByTokenResponse(true, null, member.getExam().getId());
    }

    @Transactional
    public void declineInvite(Long examId, Long userId) {
        ExamMember member = examMemberRepository
            .findByExamIdAndUserId(examId, userId)
            .orElseThrow(() -> new EntityNotFoundException("Invite not found"));

        if (member.getStatus() != ExamMemberStatus.PENDING) {
            throw new IllegalStateException("No pending invite for this exam");
        }

        member.setStatus(ExamMemberStatus.DECLINED);
        
        // Delete invite notification
        notificationRepository.deleteExamInviteNotification(
            userId,
            NotificationType.EXAM_INVITE.name(),
            examId.toString()
        );
    }

    public List<InviteResultResponse.PendingInviteExamResponse> getPendingInvites(Long userId) {
        return examMemberRepository
            .findByUserIdAndStatus(userId, ExamMemberStatus.PENDING)
            .stream()
            .map(m -> new InviteResultResponse.PendingInviteExamResponse(
                m.getExam().getId(),
                m.getExam().getTitle(),
                m.getRole().name(),
                m.getExam().getSet().getId(),
                m.getCreatedAt()
            ))
            .toList();
    }

    public Page<NoteMemberResponse> getMembers(Long examId, Pageable pageable) {
        return examMemberRepository
            .findPagedByExamId(examId, pageable)
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
        Long examId, String keyword, Pageable pageable) {
        return examMemberRepository
            .searchMembers(examId, keyword, pageable)
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
    public void removeMember(Long examId, Long targetUserId, Long requesterId) {
        if (!isOwner(requesterId, examId)) {
            throw new AccessDeniedException("Only owner can remove members");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Owner cannot remove themselves");
        }
        examMemberRepository.deleteByExamIdAndUserId(examId, targetUserId);
    }

    @Transactional
    public void updateMemberRole(Long examId, Long targetUserId, NoteRole newRole, Long requesterId) {
        if (!isOwner(requesterId, examId)) {
            throw new AccessDeniedException("Only owner can change member roles");
        }
        if (targetUserId.equals(requesterId)) {
            throw new IllegalStateException("Cannot change your own role");
        }
        if (newRole == NoteRole.OWNER) {
            throw new IllegalArgumentException("Cannot assign OWNER role");
        }

        ExamMember member = examMemberRepository
            .findByExamIdAndUserIdAndStatus(examId, targetUserId, ExamMemberStatus.ACTIVE)
            .orElseThrow(() -> new EntityNotFoundException("Member not found"));

        member.setRole(newRole);
    }

    private void doInvite(Long examId, Long targetUserId, NoteRole role) {
        String token = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusHours(inviteTokenExpiryHours);

        examMemberRepository.findByExamIdAndUserId(examId, targetUserId)
            .ifPresentOrElse(
                existing -> {
                    if (existing.getStatus() == ExamMemberStatus.ACTIVE) {
                        throw new IllegalStateException("User is already a member");
                    }
                    existing.setRole(role);
                    existing.setStatus(ExamMemberStatus.PENDING);
                    existing.setInviteToken(token);
                    existing.setInviteTokenExpiresAt(expiresAt);
                },
                () -> {
                    ExamMember member = new ExamMember();
                    member.setExam(examRepository.getReferenceById(examId));
                    member.setUser(userRepository.getReferenceById(targetUserId));
                    member.setRole(role);
                    member.setStatus(ExamMemberStatus.PENDING);
                    member.setInviteToken(token);
                    member.setInviteTokenExpiresAt(expiresAt);
                    examMemberRepository.save(member);
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

    @Async
    protected void sendInviteNotifications(
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
        data.put("expiresAt", expiresAt);

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

        userIds.forEach(userId ->
            examMemberRepository.findByExamIdAndUserId(examId, userId)
                .ifPresent(member -> {
                    String acceptUrl = frontendUrl
                        + "/exam-invites/accept?token=" + member.getInviteToken();

                    emailService.sendExamInviteNotification(
                        member.getUser().getEmail(),
                        inviterName,
                        examTitle,
                        member.getRole().name(),
                        acceptUrl
                    );
                })
        );
    }

    public Page<UserSearchResponse> searchUsers(String keyword, Long examId, Pageable pageable) {
        return userRepository
            .searchByNameOrEmailForExam(keyword, examId, pageable)
            .map(UserSearchResponse::from);
    }

    public NoteRole getUserRoleInExam(Long examId, Long userId) {
        // 1. Try to find user as a member
        var optionalMember = examMemberRepository
            .findByExamIdAndUserIdAndStatus(examId, userId, ExamMemberStatus.ACTIVE);
        
        if (optionalMember.isPresent()) {
            // User is a member: return their actual role
            return optionalMember.get().getRole();
        }
        
        // 2. User is not a member
        if (isExamPublic(examId)) {
            // PUBLIC exam: non-member = VIEWER role
            return NoteRole.VIEWER;
        }
        
        // PRIVATE exam: non-member = AccessDenied
        throw new AccessDeniedException(
            "User " + userId + " has no access to exam " + examId);
    }
}
