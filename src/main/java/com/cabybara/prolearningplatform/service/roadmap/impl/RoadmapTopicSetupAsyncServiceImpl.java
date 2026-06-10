package com.cabybara.prolearningplatform.service.roadmap.impl;

import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiRequestDto;
import com.cabybara.prolearningplatform.dto.internal.roadmap.TopicContentAiResponseDto;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.note.Note;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapTopic;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.repository.RoadmapTopicRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.service.ai.AIRoadmapService;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.service.roadmap.RoadmapTopicSetupAsyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class RoadmapTopicSetupAsyncServiceImpl implements RoadmapTopicSetupAsyncService {

    private final RoadmapTopicRepository roadmapTopicRepository;
    private final SetRepository setRepository;
    private final NoteRepository noteRepository;
    private final NotePermissionService notePermissionService;
    private final AIRoadmapService aiRoadmapService;
    private final CacheManager cacheManager;

    @Override
    @Async("heavyTaskExecutor")
    @Transactional
    public void generateTopicContent(Long userId, Long topicId, Long setId, String topicTitle,
                                     String description, String chapterTitle, String chapterObjective,
                                     Long roadmapId, String roadmapTitle) {
        try {
            log.info("Generating content for topic {} ('{}') in roadmap '{}'", topicId, topicTitle, roadmapTitle);

            RoadmapTopic topic = roadmapTopicRepository.findById(topicId).orElse(null);
            if (topic == null) {
                log.warn("Topic {} not found during async content generation — skipping", topicId);
                return;
            }

            Set set = setRepository.findById(setId).orElse(null);
            if (set == null) {
                log.warn("Set {} not found for topic {} — marking FAILED", setId, topicId);
                markFailed(topic);
                return;
            }

            TopicContentAiResponseDto aiResponse = callAiForTopicContent(
                    topicTitle, description, chapterTitle, chapterObjective, roadmapTitle, roadmapId);

            Note savedNote = noteRepository.save(Note.builder()
                    .title(topicTitle)
                    .content(aiResponse.getContent())
                    .privacy(Privacy.PRIVATE)
                    .set(set)
                    .user(set.getUser())
                    .roadmapTopic(topic)
                    .build());

            notePermissionService.addOwner(savedNote.getId(), userId);
            setRepository.updateLastModifiedDate(setId, OffsetDateTime.now());

            topic.setSummary(aiResponse.getSummary());
            topic.setContentStatus(TopicContentStatus.READY);
            roadmapTopicRepository.save(topic);
            evictRoadmapDetailCache(userId, roadmapId);

            log.info("Topic {} content generated and set to READY", topicId);

        } catch (Exception e) {
            log.error("Async content generation failed for topic {}: {}", topicId, e.getMessage());
            roadmapTopicRepository.findById(topicId).ifPresent(topic -> {
                markFailed(topic);
                evictRoadmapDetailCache(userId, roadmapId);
            });
        }
    }

    private TopicContentAiResponseDto callAiForTopicContent(String topicTitle, String description,
                                                             String chapterTitle, String chapterObjective,
                                                             String roadmapTitle, Long roadmapId) {
        List<TopicContentAiRequestDto.SummaryContextDto> previousSummaries =
                roadmapTopicRepository.findTopicsWithSummaryByRoadmapId(roadmapId).stream()
                        .map(t -> TopicContentAiRequestDto.SummaryContextDto.builder()
                                .title(t.getTitle())
                                .summary(t.getSummary())
                                .build())
                        .toList();

        TopicContentAiRequestDto request = TopicContentAiRequestDto.builder()
                .topicTitle(topicTitle)
                .description(description)
                .chapterTitle(chapterTitle)
                .chapterObjective(chapterObjective)
                .roadmapTitle(roadmapTitle)
                .previousSummaries(previousSummaries)
                .build();

        return aiRoadmapService.generateTopicContent(request);
    }

    private void markFailed(RoadmapTopic topic) {
        topic.setContentStatus(TopicContentStatus.FAILED);
        roadmapTopicRepository.save(topic);
    }

    private void evictRoadmapDetailCache(Long userId, Long roadmapId) {
        Cache cache = cacheManager.getCache("roadmap_detail");
        if (cache != null) {
            cache.evict(userId + ":" + roadmapId);
        }
    }
}
