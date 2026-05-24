package com.cabybara.prolearningplatform.enums;

public enum ResourceType {
    ALL,
    NOTE,
    FLASHCARD,
    EXAM;

    public ContentType toContentType() {
        return switch (this) {
            case NOTE -> ContentType.NOTE;
            case FLASHCARD -> ContentType.FLASHCARD;
            case EXAM -> ContentType.EXAM;
            case ALL -> null;
        };
    }
}
