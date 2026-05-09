package com.cabybara.prolearningplatform.service.roadmap.impl;

import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapTopic;
import com.cabybara.prolearningplatform.repository.RoadmapTopicRepository;
import com.cabybara.prolearningplatform.service.roadmap.RoadmapTopicSetupAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoadmapTopicSetupAsyncServiceImpl implements RoadmapTopicSetupAsyncService {

    private final RoadmapTopicRepository roadmapTopicRepository;

    @Override
    @Async("heavyTaskExecutor")
    public void generateTopicContent(Long topicId, Long setId, String topicTitle,
                                     String description, String chapterTitle, String chapterObjective) {
        try {
            log.info("Setting up content for topic {} (mock — AI call not yet implemented)", topicId);

            // TODO: call AIRoadmapService.generateTopicContent() and create a Note in setId
            // For now: mark topic content as READY so FE can proceed
            RoadmapTopic topic = roadmapTopicRepository.findById(topicId).orElse(null);
            if (topic == null) {
                log.warn("Topic {} not found during async content setup — transaction may not have committed yet", topicId);
                return;
            }
            topic.setContentStatus(TopicContentStatus.READY);
            roadmapTopicRepository.save(topic);

            log.info("Topic {} content status set to READY", topicId);
        } catch (Exception e) {
            log.error("Async content setup failed for topic {}: {}", topicId, e.getMessage());
            roadmapTopicRepository.findById(topicId).ifPresent(t -> {
                t.setContentStatus(TopicContentStatus.FAILED);
                roadmapTopicRepository.save(t);
            });
        }
    }
}
