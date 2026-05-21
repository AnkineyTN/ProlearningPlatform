package com.cabybara.prolearningplatform.scheduler;

import com.cabybara.prolearningplatform.repository.ExamMemberRepository;
import com.cabybara.prolearningplatform.repository.FlashcardMemberRepository;
import com.cabybara.prolearningplatform.repository.NoteMemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PermissionCleanupScheduler {
    private static final String SCHEDULER_TIME_ZONE = "Asia/Ho_Chi_Minh";
    private static final int CLEANUP_DAYS = 7;

    private final NoteMemberRepository noteMemberRepository;
    private final ExamMemberRepository examMemberRepository;
    private final FlashcardMemberRepository flashcardMemberRepository;

    @Transactional
    @Scheduled(cron = "${schedule.permission-cleanup.cron:0 0 2 * * *}", zone = SCHEDULER_TIME_ZONE)
    public void cleanupOldInviteRecords() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(CLEANUP_DAYS);

        try {
            noteMemberRepository.deleteOldPendingOrDeclinedRecords(cutoffDate);
            log.info("[note-invite-cleanup] Cleaned up old PENDING/DECLINED records older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("[note-invite-cleanup] Error cleaning up old invite records: {}", e.getMessage(), e);
        }

        try {
            examMemberRepository.deleteOldPendingOrDeclinedRecords(cutoffDate);
            log.info("[exam-invite-cleanup] Cleaned up old PENDING/DECLINED records older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("[exam-invite-cleanup] Error cleaning up old invite records: {}", e.getMessage(), e);
        }

        try {
            flashcardMemberRepository.deleteOldPendingOrDeclinedRecords(cutoffDate);
            log.info("[flashcard-invite-cleanup] Cleaned up old PENDING/DECLINED records older than {}", cutoffDate);
        } catch (Exception e) {
            log.error("[flashcard-invite-cleanup] Error cleaning up old invite records: {}", e.getMessage(), e);
        }
    }
}
