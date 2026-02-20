package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.set.SetUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.request.set.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.response.set.SetResponseDto;
import com.cabybara.prolearningplatform.mapper.helpers.DateTimeMapper;
import com.cabybara.prolearningplatform.model.Flashcard;
import com.cabybara.prolearningplatform.model.Note;
import org.mapstruct.*;

import com.cabybara.prolearningplatform.model.Set;

import java.util.List;

@Mapper(componentModel = "spring", uses = {DateTimeMapper.class})
public interface SetMapper {
    Set fromSetCreationRequestDto(SetCreationRequestDto setCreationRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSetFromDto(SetUpdatingRequestDto setUpdatingRequestDto, @MappingTarget Set targetSet);

    @Mapping(source = "notes", target = "numNotes", qualifiedByName = "listNotesToNumNotes")
    @Mapping(source = "flashcards", target = "numFlashcards", qualifiedByName = "listFlashcardsToNumFlashcards")
    SetResponseDto toSetResponseDto(Set set);

    @Named("listNotesToNumNotes")
    default Long numNotesMapping(List<Note> listNotes) {
        return (long) listNotes.size();
    }

    @Named("listFlashcardsToNumFlashcards")
    default Long numFlashcardsMapping(List<Flashcard> listFlashcards) {
        return (long) listFlashcards.size();
    }
}
