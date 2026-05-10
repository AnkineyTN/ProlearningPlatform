package com.cabybara.prolearningplatform.service.roadmap;

public interface RoadmapTopicSetupAsyncService {

    void generateTopicContent(Long topicId, Long setId, String topicTitle,
                              String description, String chapterTitle, String chapterObjective,
                              Long roadmapId, String roadmapTitle);
}
