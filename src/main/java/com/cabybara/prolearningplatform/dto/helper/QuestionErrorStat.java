package com.cabybara.prolearningplatform.dto.helper;

public interface QuestionErrorStat {
    Long getQuestionId();
    String getQuestionText();
    Long getTotalAttempts();
    Long getIncorrectCount();
}
