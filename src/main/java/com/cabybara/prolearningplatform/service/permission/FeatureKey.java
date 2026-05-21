package com.cabybara.prolearningplatform.service.permission;

/**
 * Danh sách các tính năng PRO của hệ thống.
 *
 * <p>Dùng các hằng số này trong {@code @PreAuthorize} để tránh hardcode string:
 * <pre>{@code
 * @PreAuthorize("@accountPermissionService.canAccessFeature(@authenticationContext.getCurrentUserId(), T(com.cabybara.prolearningplatform.service.permission.FeatureKey).AI_GENERATION)")
 * }</pre>
 *
 * <p>Hoặc đơn giản hơn, dùng trực tiếp string constant:
 * <pre>{@code
 * @PreAuthorize("@accountPermissionService.canAccessFeature(@authenticationContext.getCurrentUserId(), 'AI_GENERATION')")
 * }</pre>
 */
public final class FeatureKey {

    private FeatureKey() {}

    // AI Features
    /** Tạo Flashcard/Note/Exam bằng AI. */
    public static final String AI_GENERATION       = "AI_GENERATION";

    // Collaboration Features
    /** Mời thành viên cộng tác vào Note/Flashcard/Exam. */
    public static final String COLLABORATION        = "COLLABORATION";

    // Analytics Features
    /** Xem báo cáo thống kê chi tiết (Activity Heatmap nâng cao, v.v.). */
    public static final String ADVANCED_ANALYTICS   = "ADVANCED_ANALYTICS";

    // Storage Features
    /** Upload file đính kèm vào Note. */
    public static final String FILE_ATTACHMENT      = "FILE_ATTACHMENT";

    // Thêm key mới tại đây khi có tính năng PRO mới
}
