package com.cabybara.prolearningplatform.dto.helper;

/** Ánh xạ roadmap -> set, dùng để gắn setId vào list roadmap mà không load full Set (tránh N+1). */
public interface RoadmapSetRef {
    Long getRoadmapId();

    Long getSetId();
}
