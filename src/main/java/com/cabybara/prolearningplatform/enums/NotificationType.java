package com.cabybara.prolearningplatform.enums;

public enum NotificationType {
    CARD_DUE_REMINDER("Cards are due for review"),
    STUDY_SESSION_REMINDER("Time to study!"),

    // Social (future)
//    SET_SHARED("Someone shared a set with you"),
//    NEW_FOLLOWER("New follower"),

    // System
    SYSTEM_ANNOUNCEMENT("System announcement"),
    ACCOUNT_ACTIVITY("Account activity"),

    // General
    GENERAL("General notification");

    private final String defaultTitle;

    NotificationType(String defaultTitle) {
        this.defaultTitle = defaultTitle;
    }

    public String getDefaultTitle() {
        return defaultTitle;
    }
}

