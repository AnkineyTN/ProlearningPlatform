package com.cabybara.prolearningplatform.service.social.impl;

import com.cabybara.prolearningplatform.enums.ContentType;
import com.cabybara.prolearningplatform.model.ResourceViewLog;
import com.cabybara.prolearningplatform.repository.ResourceViewLogRepository;
import com.cabybara.prolearningplatform.service.social.ResourceViewLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceViewLogServiceImpl implements ResourceViewLogService {

    private final ResourceViewLogRepository viewLogRepository;

    @Override
    @Async
    @Transactional
    public void recordView(Long resourceId, ContentType resourceType, String viewerIp, Long userId) {
        try {
            OffsetDateTime oneHourAgo = OffsetDateTime.now().minusHours(1);
            boolean alreadyViewed = viewLogRepository
                    .existsByResourceIdAndResourceTypeAndViewerIpAndViewedAtAfter(
                            resourceId, resourceType, viewerIp, oneHourAgo);

            if (alreadyViewed) {
                log.debug("View already recorded in the last hour for resourceId: {}, type: {}, IP: {}", 
                        resourceId, resourceType, viewerIp);
                return;
            }

            ResourceViewLog logEntity = ResourceViewLog.builder()
                    .resourceId(resourceId)
                    .resourceType(resourceType)
                    .viewerIp(viewerIp)
                    .userId(userId)
                    .build();

            viewLogRepository.save(logEntity);
            log.info("Successfully recorded public view for resourceId: {}, type: {}, IP: {}", 
                    resourceId, resourceType, viewerIp);
        } catch (Exception e) {
            log.error("Failed to record resource view log", e);
        }
    }
}
