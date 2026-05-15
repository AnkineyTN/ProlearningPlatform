package com.cabybara.prolearningplatform.enums.AI;

public enum RoadmapLevel {
    BEGINNER("beginner"),
    INTERMIDIATE("intermidiate"),
    ADVANCED("advanced");

    private String desc;

    RoadmapLevel(String desc) {
        this.desc = desc;
    }

    public String getDescription() {
        return desc;
    }
}
