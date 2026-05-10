package com.cabybara.prolearningplatform.dto.response.roadmap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TopicCompleteResponseDto {

    private Long completedTopicId;
    private boolean chapterCompleted;
    private Long nextUnlockedChapterId;
}
