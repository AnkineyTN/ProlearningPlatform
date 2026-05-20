package com.cabybara.prolearningplatform.service.permission.impl;

import com.cabybara.prolearningplatform.enums.AccountType;
import com.cabybara.prolearningplatform.repository.ExamRepository;
import com.cabybara.prolearningplatform.repository.FlashcardRepository;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.permission.AccountPermissionService;
import com.cabybara.prolearningplatform.service.permission.FeatureKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Set;

/**
 * Triển khai kiểm soát quyền dựa trên {@link AccountType} của User.
 *
 * <h3>Cách sử dụng trong Controller:</h3>
 *
 * <p><b>1. Kiểm tra tài khoản PRO:</b>
 * <pre>{@code
 * @PreAuthorize("isAuthenticated() and @accountPermissionService.isPro(@authenticationContext.getCurrentUserId())")
 * }</pre>
 *
 * <p><b>2. Kiểm tra quota tạo tài nguyên:</b>
 * <pre>{@code
 * @PreAuthorize("isAuthenticated() and @accountPermissionService.canCreateNote(@authenticationContext.getCurrentUserId())")
 * }</pre>
 *
 * <p><b>3. Kiểm tra tính năng PRO cụ thể (dùng {@link FeatureKey}):</b>
 * <pre>{@code
 * @PreAuthorize("isAuthenticated() and @accountPermissionService.canAccessFeature(@authenticationContext.getCurrentUserId(), 'AI_GENERATION')")
 * }</pre>
 */
@Slf4j
@Service("accountPermissionService")
@RequiredArgsConstructor
public class AccountPermissionServiceImpl implements AccountPermissionService {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final FlashcardRepository flashcardRepository;
    private final ExamRepository examRepository;

    // Quota limits (có thể override qua application.properties)
    @Value("${app.quota.free.max-notes:10}")
    private int freeMaxNotes;

    @Value("${app.quota.free.max-flashcards:10}")
    private int freeMaxFlashcards;

    @Value("${app.quota.free.max-exams:5}")
    private int freeMaxExams;

    // Tập hợp các tính năng chỉ dành riêng cho tài khoản PRO.
    // Thêm key vào đây khi có tính năng PRO mới.
    private static final Set<String> PRO_ONLY_FEATURES = Set.of(
        FeatureKey.AI_GENERATION,
        FeatureKey.COLLABORATION,
        FeatureKey.ADVANCED_ANALYTICS,
        FeatureKey.FILE_ATTACHMENT
    );

    // Account Type Checks
    @Override
    public boolean isPro(Long userId) {
        return getAccountType(userId) == AccountType.PRO;
    }

    @Override
    public boolean isFree(Long userId) {
        return getAccountType(userId) == AccountType.FREE;
    }

    // Resource Quota Checks
    @Override
    public boolean canCreateNote(Long userId) {
        if (isPro(userId)) return true;

        long current = noteRepository.countNumOfNoteByCreatedUser(userId);
        boolean allowed = current < freeMaxNotes;

        if (!allowed) {
            log.debug("[AccountPermission] User {} hit note quota ({}/{})", userId, current, freeMaxNotes);
        }
        return allowed;
    }

    @Override
    public boolean canCreateFlashcard(Long userId) {
        if (isPro(userId)) return true;

        long current = flashcardRepository.countNumOfFlashcardByCreatedUser(userId);
        boolean allowed = current < freeMaxFlashcards;

        if (!allowed) {
            log.debug("[AccountPermission] User {} hit flashcard quota ({}/{})", userId, current, freeMaxFlashcards);
        }
        return allowed;
    }

    @Override
    public boolean canCreateExam(Long userId) {
        if (isPro(userId)) return true;

        long current = examRepository.countNumOfExamByCreatedBy(userId);
        boolean allowed = current < freeMaxExams;

        if (!allowed) {
            log.debug("[AccountPermission] User {} hit exam quota ({}/{})", userId, current, freeMaxExams);
        }
        return allowed;
    }

    // Feature Access Checks
    @Override
    public boolean canAccessFeature(Long userId, String featureKey) {
        // Tính năng không yêu cầu PRO → tất cả đều truy cập được
        if (!PRO_ONLY_FEATURES.contains(featureKey)) {
            log.warn("[AccountPermission] Unknown featureKey '{}', defaulting to ALLOW", featureKey);
            return true;
        }
        return isPro(userId);
    }

    // Lấy AccountType của user từ DB. Trả về FREE nếu user không tìm thấy.
    private AccountType getAccountType(Long userId) {
        return userRepository.findById(userId)
            .map(u -> u.getAccountType() != null ? u.getAccountType() : AccountType.FREE)
            .orElse(AccountType.FREE);
    }
}
