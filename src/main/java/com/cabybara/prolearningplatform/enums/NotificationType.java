package com.cabybara.prolearningplatform.enums;

public enum NotificationType {
    CARD_DUE_REMINDER("Cards are due for review"),
    STUDY_SESSION_REMINDER("Time to study!"),
    WEEKLY_SUMMARY("Weekly study summary"),

    NOTE_INVITE("You have been invited to collaborate on a note"),
    NOTE_INVITE_ACCEPTED("Your invite to a note has been accepted"),
    
    FLASHCARD_INVITE("You have been invited to collaborate on a flashcard"),
    EXAM_INVITE("You have been invited to collaborate on an exam"),

    // Social (future)
//    SET_SHARED("Someone shared a set with you"),
//    NEW_FOLLOWER("New follower"),

    // Account moderation
    ACCOUNT_BLOCKED("Your account has been suspended"),
    ACCOUNT_UNBLOCKED("Your account has been reinstated"),
    ACCOUNT_UPGRADED("Your account has been upgraded to PRO"),

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

