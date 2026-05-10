package com.cabybara.prolearningplatform.model.roadmap;

import com.cabybara.prolearningplatform.enums.RoadmapStatus;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roadmaps")
public class Roadmap extends AbstractEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(length = 255, nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column(name = "estimated_total_hours")
    private Integer estimatedTotalHours;

    @Column(length = 20, nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private RoadmapStatus status = RoadmapStatus.ACTIVE;
}
