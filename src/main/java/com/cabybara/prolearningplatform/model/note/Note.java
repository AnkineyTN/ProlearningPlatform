package com.cabybara.prolearningplatform.model.note;

import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.roadmap.RoadmapTopic;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "note")
public class Note extends AbstractEntity {
    @Column(name = "note_url")
    private String noteUrl;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Privacy privacy;

    @Column(length = 50)
    private String status;

    @Column(name = "yjs_state", columnDefinition = "BYTEA")
    private byte[] yjsState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_set", referencedColumnName = "id")
    private Set set;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_user", referencedColumnName = "id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "roadmap_topic_id")
    private RoadmapTopic roadmapTopic;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoteDocs> noteDocs;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NoteImgs> noteImgs;
}
