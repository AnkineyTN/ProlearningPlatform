package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.set.SetUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.set.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.response.set.SetResponseDto;
import com.cabybara.prolearningplatform.dto.response.set.SetSummaryResponseDto;
import com.cabybara.prolearningplatform.mapper.helpers.DateTimeMapper;
import com.cabybara.prolearningplatform.model.exam.Exam;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import com.cabybara.prolearningplatform.model.note.Note;
import org.mapstruct.*;

import com.cabybara.prolearningplatform.model.Set;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateTimeMapper.class})
public interface SetMapper {
    Set fromSetCreationRequestDto(SetCreationRequestDto setCreationRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSetFromDto(SetUpdatingRequestDto setUpdatingRequestDto, @MappingTarget Set targetSet);

    @Mapping(source = "notes", target = "numNotes", qualifiedByName = "listToCount")
    @Mapping(source = "flashcards", target = "numFlashcards", qualifiedByName = "listToCount")
    @Mapping(source = "exams", target = "numExams", qualifiedByName = "listToCount")
    SetResponseDto toSetResponseDto(Set set);

    SetSummaryResponseDto toSetSummaryResponseDto(Set set);

    @Named("listToCount")
    default Long listToCount(List<?> list) {
        return list == null ? 0L : (long) list.size();
    }
}
