package com.cabybara.prolearningplatform.service.ai;

import com.cabybara.prolearningplatform.dto.internal.CardContent;
import com.cabybara.prolearningplatform.dto.internal.QuestionContent;
import com.cabybara.prolearningplatform.dto.request.exam.*;
import com.cabybara.prolearningplatform.dto.response.exam.EssayGradingResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.ExplainWrongAnswerResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.GenerateExamByAIResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.GenerateFlashcardByAIResponseDto;
import com.cabybara.prolearningplatform.enums.Language;

import java.util.List;
import java.util.Map;

public interface AIExamService {
    public GenerateExamByAIResponseDto generateExamByFiles(GenerateExamByFileRequestDto request);

    public GenerateExamByAIResponseDto generateExamByNotes(AIGenerateExamByNoteRequestDto request);

    public GenerateExamByAIResponseDto generateExamByWeb(GenerateExamByWebRequestDto request);

    public GenerateExamByAIResponseDto generateExamByExistingExam(GenerateExamByExistingExamRequestDto request);

    EssayGradingResponseDto gradeEssay(EssayGradingRequestDto request);

    ExplainWrongAnswerResponseDto explainWrongAnswer(ExplainWrongAnswerRequestDto request);

    CreateExamFromReviewRequestDto generateExamFromCards(List<CardContent> cards);

    CreateExamFromReviewRequestDto generateExamFromQuestions(List<QuestionContent> questions);
}
