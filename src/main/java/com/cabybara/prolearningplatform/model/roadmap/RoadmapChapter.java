package com.cabybara.prolearningplatform.model.roadmap;

import com.cabybara.prolearningplatform.enums.ChapterStatus;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roadmap_chapters")
public class RoadmapChapter extends AbstractEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private Roadmap roadmap;

    @Column(name = "chapter_key", length = 50, nullable = false)
    private String chapterKey;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String objective;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ChapterStatus status = ChapterStatus.LOCKED;
}
