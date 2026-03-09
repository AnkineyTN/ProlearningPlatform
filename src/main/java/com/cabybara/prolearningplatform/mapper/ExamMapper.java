package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.exam.*;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamMapper {
    Quiz toQuiz(CreateQuizRequestDto createQuizRequestDto);

    QuizResponseDto toQuizResponseDto(Quiz quiz);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "set", ignore = true)
    @Mapping(target = "quizQuestions", ignore = true)
    void updateQuizFromDto(UpdateQuizRequestDto updateQuizRequestDto, @MappingTarget Quiz quiz);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Question toQuestion(CreateQuestionRequestDto createQuestionRequestDto);

    QuestionResponseDto toQuestionResponseDto(Question question);

    QuestionOptionDto toQuestionOptionDto(QuestionOption questionOption);

    List<QuestionOptionDto> toQuestionOptionDtoList(List<QuestionOption> options);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "question", ignore = true)
    QuestionOption toQuestionOption(QuestionOptionDto questionOptionDto);

    List<QuestionOption> toQuestionOptionList(List<QuestionOptionDto> optionDtos);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "options", ignore = true)
    void updateQuestionFromDto(UpdateQuestionRequestDto dto, @MappingTarget Question question);
}
