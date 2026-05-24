package com.cabybara.prolearningplatform.service.roadmap.impl;

import com.cabybara.prolearningplatform.dto.request.roadmap.AcceptRoadmapRequestDto;
import com.cabybara.prolearningplatform.dto.request.roadmap.RoadmapPreviewRequestDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapDetailResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapListItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.RoadmapPreviewResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.TopicCompleteResponseDto;
import com.cabybara.prolearningplatform.dto.response.roadmap.TopicStartResponseDto;
import com.cabybara.prolearningplatform.enums.ChapterStatus;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.enums.RoadmapStatus;
import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.roadmap.Roadmap;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapChapter;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapTopic;
import com.cabybara.prolearningplatform.repository.RoadmapChapterRepository;
import com.cabybara.prolearningplatform.repository.RoadmapRepository;
import com.cabybara.prolearningplatform.repository.RoadmapTopicRepository;
import com.cabybara.prolearningplatform.repository.SetRepository;
import com.cabybara.prolearningplatform.repository.UserRepository;
import com.cabybara.prolearningplatform.service.ai.AIRoadmapService;
import com.cabybara.prolearningplatform.service.roadmap.RoadmapService;
import com.cabybara.prolearningplatform.service.roadmap.RoadmapTopicSetupAsyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoadmapServiceImpl implements RoadmapService {

    private final RoadmapRepository roadmapRepository;
    private final RoadmapChapterRepository roadmapChapterRepository;
    private final RoadmapTopicRepository roadmapTopicRepository;
    private final SetRepository setRepository;
    private final UserRepository userRepository;
    private final AIRoadmapService aiRoadmapService;
    private final RoadmapTopicSetupAsyncService topicSetupAsyncService;

    @Override
    public RoadmapPreviewResponseDto previewRoadmap(RoadmapPreviewRequestDto request) {
        return aiRoadmapService.generateRoadmap(request);
    }

    @Override
    @Transactional
    public RoadmapDetailResponseDto acceptRoadmap(Long userId, AcceptRoadmapRequestDto dto) {
        Roadmap savedRoadmap = createAndSaveRoadmap(userId, dto);

        List<AcceptRoadmapRequestDto.ChapterDto> chapterDtos = dto.getChapters();
        for (int ci = 0; ci < chapterDtos.size(); ci++) {
            createChapterWithTopics(savedRoadmap, chapterDtos.get(ci), ci);
        }

        return buildDetailResponse(savedRoadmap,
                roadmapChapterRepository.findByRoadmapIdOrderByOrderIndex(savedRoadmap.getId()),
                roadmapTopicRepository.findAllByRoadmapIdOrdered(savedRoadmap.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoadmapListItemResponseDto> getRoadmaps(Long userId, Pageable pageable) {
        return roadmapRepository.findByUserId(userId, pageable)
                .map(roadmap -> {
                    long total = roadmapTopicRepository.countByRoadmapId(roadmap.getId());
                    long completed = roadmapTopicRepository.countCompletedByRoadmapId(roadmap.getId());
                    long totalChapters = roadmapChapterRepository.countByRoadmapId(roadmap.getId());
                    long completedChapters = roadmapChapterRepository.countByRoadmapIdAndStatus(roadmap.getId(), ChapterStatus.COMPLETED);

                    return RoadmapListItemResponseDto.builder()
                            .id(roadmap.getId())
                            .title(roadmap.getTitle())
                            .overview(roadmap.getOverview())
                            .status(roadmap.getStatus())
                            .estimatedTotalHours(roadmap.getEstimatedTotalHours())
                            .totalTopics(total)
                            .completedTopics(completed)
                            .progressPercent(total == 0 ? 0 : (int) (completed * 100 / total))
                            .totalChapters((int) totalChapters)
                            .completedChapters((int) completedChapters)
                            .createdAt(roadmap.getCreatedAt())
                            .build();
                });
    }

    @Override
    @Cacheable(value = "roadmap_detail", key = "#userId + ':' + #roadmapId")
    @Transactional(readOnly = true)
    public RoadmapDetailResponseDto getRoadmap(Long userId, Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findByIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));

        List<RoadmapChapter> chapters = roadmapChapterRepository.findByRoadmapIdOrderByOrderIndex(roadmapId);
        List<RoadmapTopic> allTopics = roadmapTopicRepository.findAllByRoadmapIdOrdered(roadmapId);

        return buildDetailResponse(roadmap, chapters, allTopics);
    }

    @Override
    @Transactional
    @CacheEvict(value = "roadmap_detail", key = "#userId + ':' + #roadmapId")
    public TopicStartResponseDto startTopic(Long userId, Long roadmapId, Long topicId) {
        Roadmap roadmap = roadmapRepository.findByIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));

        RoadmapTopic topic = roadmapTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        validateTopicAccess(topic, roadmapId);

        if (topic.getSetId() != null) {
            return TopicStartResponseDto.builder()
                    .topicId(topicId)
                    .setId(topic.getSetId())
                    .contentStatus(topic.getContentStatus())
                    .build();
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Set savedSet = createSetForTopic(topic, user);
        topic.setSetId(savedSet.getId());
        topic.setContentStatus(TopicContentStatus.GENERATING);
        roadmapTopicRepository.save(topic);

        scheduleContentGeneration(userId, topicId, savedSet.getId(), topic, topic.getChapter(), roadmap);

        return TopicStartResponseDto.builder()
                .topicId(topicId)
                .setId(savedSet.getId())
                .contentStatus(TopicContentStatus.GENERATING)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "roadmap_detail", key = "#userId + ':' + #roadmapId")
    public TopicCompleteResponseDto markTopicComplete(Long userId, Long roadmapId, Long topicId) {
        Roadmap roadmap = roadmapRepository.findByIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));

        RoadmapTopic topic = roadmapTopicRepository.findById(topicId)
                .orElseThrow(() -> new ResourceNotFoundException("Topic not found: " + topicId));

        validateTopicAccess(topic, roadmapId);
        RoadmapChapter chapter = topic.getChapter();

        if (topic.getCompleted()) {
            return TopicCompleteResponseDto.builder()
                    .completedTopicId(topicId)
                    .chapterCompleted(chapter.getStatus() == ChapterStatus.COMPLETED)
                    .nextUnlockedChapterId(null)
                    .build();
        }

        topic.setCompleted(true);
        roadmapTopicRepository.save(topic);

        boolean chapterCompleted = isChapterComplete(chapter.getId());
        Long nextUnlockedChapterId = chapterCompleted
                ? advanceChapterProgress(roadmap, roadmapId, chapter)
                : null;

        return TopicCompleteResponseDto.builder()
                .completedTopicId(topicId)
                .chapterCompleted(chapterCompleted)
                .nextUnlockedChapterId(nextUnlockedChapterId)
                .build();
    }

    @Override
    @Transactional
    @CacheEvict(value = "roadmap_detail", key = "#userId + ':' + #roadmapId")
    public void abandonRoadmap(Long userId, Long roadmapId) {
        Roadmap roadmap = roadmapRepository.findByIdAndUserId(roadmapId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap not found: " + roadmapId));

        roadmap.setStatus(RoadmapStatus.ABANDONED);
        roadmapRepository.save(roadmap);
    }

    private void validateTopicAccess(RoadmapTopic topic, Long roadmapId) {
        RoadmapChapter chapter = topic.getChapter();
        if (!chapter.getRoadmap().getId().equals(roadmapId)) {
            throw new AccessDeniedException("Topic does not belong to this roadmap");
        }
        if (chapter.getStatus() != ChapterStatus.IN_PROGRESS) {
            throw new AccessDeniedException("Chapter is not accessible");
        }
    }

    private Set createSetForTopic(RoadmapTopic topic, User user) {
        return setRepository.save(Set.builder()
                .title(topic.getTitle())
                .description(topic.getDescription())
                .privacy(Privacy.PRIVATE)
                .user(user)
                .notes(new ArrayList<>())
                .flashcards(new ArrayList<>())
                .build());
    }

    private void scheduleContentGeneration(Long userId, Long topicId, Long setId,
                                            RoadmapTopic topic, RoadmapChapter chapter,
                                            Roadmap roadmap) {
        String topicTitle = topic.getTitle();
        String description = topic.getDescription();
        String chapterTitle = chapter.getTitle();
        String chapterObjective = chapter.getObjective();
        Long roadmapId = roadmap.getId();
        String roadmapTitle = roadmap.getTitle();

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                topicSetupAsyncService.generateTopicContent(userId,
                        topicId, setId, topicTitle, description,
                        chapterTitle, chapterObjective, roadmapId, roadmapTitle);
            }
        });
    }

    private Roadmap createAndSaveRoadmap(Long userId, AcceptRoadmapRequestDto dto) {
        return roadmapRepository.save(Roadmap.builder()
                .userId(userId)
                .title(dto.getRoadmapTitle())
                .overview(dto.getOverview())
                .estimatedTotalHours(dto.getEstimatedTotalHours())
                .status(RoadmapStatus.ACTIVE)
                .build());
    }

    private void createChapterWithTopics(Roadmap roadmap,
                                          AcceptRoadmapRequestDto.ChapterDto chapterDto,
                                          int chapterIndex) {
        RoadmapChapter savedChapter = roadmapChapterRepository.save(RoadmapChapter.builder()
                .roadmap(roadmap)
                .chapterKey(chapterDto.getChapterId())
                .title(chapterDto.getChapterTitle())
                .objective(chapterDto.getObjective())
                .orderIndex(chapterIndex)
                .status(chapterIndex == 0 ? ChapterStatus.IN_PROGRESS : ChapterStatus.LOCKED)
                .build());

        List<AcceptRoadmapRequestDto.TopicDto> topicDtos = chapterDto.getTopics();
        for (int ti = 0; ti < topicDtos.size(); ti++) {
            createTopicEntry(savedChapter, topicDtos.get(ti), ti);
        }
    }

    private void createTopicEntry(RoadmapChapter chapter,
                                   AcceptRoadmapRequestDto.TopicDto topicDto,
                                   int topicIndex) {
        roadmapTopicRepository.save(RoadmapTopic.builder()
                .chapter(chapter)
                .topicKey(topicDto.getTopicId())
                .title(topicDto.getTopicTitle())
                .description(topicDto.getDescription())
                .orderIndex(topicIndex)
                .completed(false)
                .contentStatus(TopicContentStatus.IDLE)
                .build());
    }

    private boolean isChapterComplete(Long chapterId) {
        long total = roadmapTopicRepository.countByChapterId(chapterId);
        long completed = roadmapTopicRepository.countByChapterIdAndCompleted(chapterId, true);
        return completed >= total;
    }

    private Long advanceChapterProgress(Roadmap roadmap, Long roadmapId, RoadmapChapter chapter) {
        chapter.setStatus(ChapterStatus.COMPLETED);
        roadmapChapterRepository.save(chapter);

        var nextChapterOpt = roadmapChapterRepository
                .findByRoadmapIdAndOrderIndex(roadmapId, chapter.getOrderIndex() + 1);

        if (nextChapterOpt.isPresent()) {
            RoadmapChapter nextChapter = nextChapterOpt.get();
            nextChapter.setStatus(ChapterStatus.IN_PROGRESS);
            roadmapChapterRepository.save(nextChapter);
            return nextChapter.getId();
        }

        roadmap.setStatus(RoadmapStatus.COMPLETED);
        roadmapRepository.save(roadmap);
        return null;
    }

    private RoadmapDetailResponseDto buildDetailResponse(Roadmap roadmap,
                                                          List<RoadmapChapter> chapters,
                                                          List<RoadmapTopic> allTopics) {
        Map<Long, List<RoadmapTopic>> topicsByChapter = allTopics.stream()
                .collect(Collectors.groupingBy(t -> t.getChapter().getId()));

        List<RoadmapDetailResponseDto.ChapterDetailDto> chapterDtos = chapters.stream()
                .map(chapter -> {
                    List<RoadmapTopic> topics = topicsByChapter.getOrDefault(chapter.getId(), List.of());
                    long chapterTotal = topics.size();
                    long chapterCompleted = topics.stream().filter(RoadmapTopic::getCompleted).count();

                    List<RoadmapDetailResponseDto.TopicDetailDto> topicDtos = topics.stream()
                            .map(t -> RoadmapDetailResponseDto.TopicDetailDto.builder()
                                    .id(t.getId())
                                    .topicKey(t.getTopicKey())
                                    .title(t.getTitle())
                                    .description(t.getDescription())
                                    .orderIndex(t.getOrderIndex())
                                    .completed(t.getCompleted())
                                    .contentStatus(t.getContentStatus())
                                    .setId(t.getSetId())
                                    .build())
                            .toList();

                    return RoadmapDetailResponseDto.ChapterDetailDto.builder()
                            .id(chapter.getId())
                            .chapterKey(chapter.getChapterKey())
                            .title(chapter.getTitle())
                            .objective(chapter.getObjective())
                            .orderIndex(chapter.getOrderIndex())
                            .status(chapter.getStatus())
                            .totalTopics(chapterTotal)
                            .completedTopics(chapterCompleted)
                            .progressPercent(chapterTotal == 0 ? 0 : (int) (chapterCompleted * 100 / chapterTotal))
                            .topics(topicDtos)
                            .build();
                })
                .toList();

        long totalTopics = allTopics.size();
        long completedTopics = allTopics.stream().filter(RoadmapTopic::getCompleted).count();

        return RoadmapDetailResponseDto.builder()
                .id(roadmap.getId())
                .title(roadmap.getTitle())
                .overview(roadmap.getOverview())
                .status(roadmap.getStatus())
                .estimatedTotalHours(roadmap.getEstimatedTotalHours())
                .totalTopics(totalTopics)
                .completedTopics(completedTopics)
                .progressPercent(totalTopics == 0 ? 0 : (int) (completedTopics * 100 / totalTopics))
                .createdAt(roadmap.getCreatedAt())
                .chapters(chapterDtos)
                .build();
    }
}
