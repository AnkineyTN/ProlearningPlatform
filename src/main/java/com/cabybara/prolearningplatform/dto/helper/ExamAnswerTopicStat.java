package com.cabybara.prolearningplatform.dto.helper;

public interface ExamAnswerTopicStat {
    Long getQuestionId();
    String getTopic();
    Boolean getIsCorrect();
    Double getEarnedPoints();
    Integer getPoints();
    String getEssayAnswer();
}
