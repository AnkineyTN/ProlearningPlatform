package com.cabybara.prolearningplatform.dto.internal;

import java.util.List;

public record QuestionContent(String questionText, List<String> options, String correctAnswer) {}
