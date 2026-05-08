package com.cabybara.prolearningplatform.service.permission;

public interface AccountPermissionService {

    // Account Type Checks
    /**
     * Kiểm tra user có phải là tài khoản PRO không.
     */
    boolean isPro(Long userId);

    /**
     * Kiểm tra user có phải là tài khoản FREE không.
     */
    boolean isFree(Long userId);

    // Resource Quota Checks
    /**
     * Kiểm tra user có thể tạo thêm Flashcard Set không.
     * FREE: giới hạn số set, PRO: không giới hạn.
     */
    boolean canCreateFlashcard(Long userId);

    /**
     * Kiểm tra user có thể tạo thêm Note không.
     * FREE: giới hạn số note, PRO: không giới hạn.
     */
    boolean canCreateNote(Long userId);

    /**
     * Kiểm tra user có thể tạo thêm Exam không.
     * FREE: giới hạn số exam, PRO: không giới hạn.
     */
    boolean canCreateExam(Long userId);

    // Feature Access Checks
    /**
     * Kiểm tra user có quyền truy cập một tính năng PRO cụ thể không.
     * Dùng FeatureKey enum để tránh typo khi dùng @PreAuthorize.
     *
     * @param featureKey tên tính năng PRO (dùng enum FeatureKey)
     */
    boolean canAccessFeature(Long userId, String featureKey);
}
