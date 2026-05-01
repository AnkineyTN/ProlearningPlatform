package com.cabybara.prolearningplatform.model.knowledge;

import com.cabybara.prolearningplatform.dto.response.knowledge.ContributingSourceDto;
import com.cabybara.prolearningplatform.dto.response.knowledge.TopicAccuracyDto;
import com.cabybara.prolearningplatform.enums.KnowledgeSourceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "knowledge_analysis")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "source_type", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private KnowledgeSourceType sourceType;

    @Column(name = "source_id", nullable = false)
    private Long sourceId;

    @Column(name = "session_ref_id")
    private Long sessionRefId;

    @Column(name = "topic_accuracies", columnDefinition = "jsonb", nullable = false)
    @JdbcTypeCode(SqlTypes.JSON)
    private List<TopicAccuracyDto> topicAccuracies;

    @Column(name = "strengths", nullable = false, columnDefinition = "TEXT")
    private String strengths;

    @Column(name = "weaknesses", nullable = false, columnDefinition = "TEXT")
    private String weaknesses;

    @Column(name = "improvements", nullable = false, columnDefinition = "TEXT")
    private String improvements;

    @Column(name = "contributing_sources", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<ContributingSourceDto> contributingSources;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
