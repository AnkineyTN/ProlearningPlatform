package com.cabybara.prolearningplatform.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

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

    // Dạng JSONB trong PostgreSQL
    @Type(JsonBinaryType.class)
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private JsonNode content;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String privacy;

    @Column(length = 50)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_set", referencedColumnName = "id")
    private Set set;
}
