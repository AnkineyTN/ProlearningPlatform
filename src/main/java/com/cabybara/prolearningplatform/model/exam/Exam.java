package com.cabybara.prolearningplatform.model.exam;

import com.cabybara.prolearningplatform.enums.CreationMethod;
import com.cabybara.prolearningplatform.enums.Privacy;
import com.cabybara.prolearningplatform.model.AbstractEntity;
import com.cabybara.prolearningplatform.model.Set;
import com.cabybara.prolearningplatform.model.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "exams")
@Getter
@Setter
public class Exam extends AbstractEntity {
    private String title;

    private String description;

    private Long duration;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    private Privacy privacy;

    @Column(name = "created_by")
    private Long createdBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "create_method", nullable = false, length = 50)
    private CreationMethod creationMethod = CreationMethod.MANUAL;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "set_id")
    private Set set;

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<ExamMember> examMembers = new ArrayList<>();

    @OneToMany(mappedBy = "exam", cascade = CascadeType.ALL)
    private List<ExamQuestion> examQuestions = new ArrayList<>();
}
