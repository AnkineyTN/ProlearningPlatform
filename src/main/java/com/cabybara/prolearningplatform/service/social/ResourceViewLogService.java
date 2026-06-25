package com.cabybara.prolearningplatform.service.social;

import com.cabybara.prolearningplatform.enums.ContentType;

public interface ResourceViewLogService {
    void recordView(Long resourceId, ContentType resourceType, String viewerIp, Long userId);
}
