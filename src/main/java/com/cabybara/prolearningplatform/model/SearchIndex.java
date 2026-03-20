package com.cabybara.prolearningplatform.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "search_index")
@Data
public class SearchIndex {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long entityId;
    private String entityType;
    private String title;
    private String description;
}