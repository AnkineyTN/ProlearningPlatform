package com.cabybara.prolearningplatform.service.permission;

public interface ResourcePermissionService {
    boolean hasAccess(Long userId, Long resourceId);
    boolean canEdit(Long userId, Long resourceId);
    boolean isOwner(Long userId, Long resourceId);
    String getRole(Long userId, Long resourceId);
}