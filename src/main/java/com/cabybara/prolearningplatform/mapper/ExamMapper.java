package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.exam.*;
import com.cabybara.prolearningplatform.dto.response.exam.ExamResponseDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuestionResponseDto;
import com.cabybara.prolearningplatform.model.exam.ExamQuestion;
import com.cabybara.prolearningplatform.model.exam.Question;
import com.cabybara.prolearningplatform.model.exam.QuestionOption;
import com.cabybara.prolearningplatform.model.exam.Exam;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ExamMapper {
    Exam toExam(CreateExamRequestDto createExamRequestDto);

    @Mapping(source = "examQuestions", target = "numQuestions", qualifiedByName = "listToCount")
    ExamResponseDto toExamResponseDto(Exam exam);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "set", ignore = true)
    @Mapping(target = "examQuestions", ignore = true)
    void updateExamFromDto(UpdateExamRequestDto updateExamRequestDto, @MappingTarget Exam exam);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "options", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    Question toQuestion(CreateQuestionRequestDto createQuestionRequestDto);

    QuestionResponseDto toQuestionResponseDto(Question question);

    @Mapping(target = ".", source = "question")
    QuestionResponseDto toQuestionResponseDto(ExamQuestion examQuestion);

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

    @Named("listToCount")
    default Long listToCount(List<?> list) {
        return list == null ? 0L : (long) list.size();
    }
}
