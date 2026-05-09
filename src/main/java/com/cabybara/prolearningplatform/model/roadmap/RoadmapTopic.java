package com.cabybara.prolearningplatform.model.roadmap;

import com.cabybara.prolearningplatform.enums.TopicContentStatus;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roadmap_topics")
public class RoadmapTopic extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id", nullable = false)
    private RoadmapChapter chapter;

    @Column(name = "topic_key", length = 50, nullable = false)
    private String topicKey;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "is_completed", nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(name = "content_status", length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TopicContentStatus contentStatus = TopicContentStatus.GENERATING;

    // Loose FK — Set exists independently; ON DELETE SET NULL at DB level
    @Column(name = "set_id")
    private Long setId;
}
