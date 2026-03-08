package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.exam.CreateQuizRequestDto;
import com.cabybara.prolearningplatform.dto.response.exam.QuizResponseDto;
import com.cabybara.prolearningplatform.model.exam.Quiz;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface QuizMapper {
    Quiz toQuiz(CreateQuizRequestDto createQuizRequestDto);

    QuizResponseDto toQuizResponseDto(Quiz quiz);
}
