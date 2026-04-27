package com.cabybara.prolearningplatform.model;

public record ResourceRef(Long id, Long setId, String title) {
    public ResourceRef(Long id, String title) {
        this(id, null, title);
    }
}
