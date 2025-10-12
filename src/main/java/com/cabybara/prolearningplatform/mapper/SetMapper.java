package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.SetUpdateRequestDto;
import com.cabybara.prolearningplatform.model.Note;
import org.mapstruct.*;
import org.mapstruct.factory.Mappers;

import com.cabybara.prolearningplatform.dto.SetCreationRequestDto;
import com.cabybara.prolearningplatform.dto.SetResponseDto;
import com.cabybara.prolearningplatform.model.Set;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SetMapper {
   SetMapper INSTANCE = Mappers.getMapper(SetMapper.class);

    Set fromSetCreationRequestDto(SetCreationRequestDto setCreationRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateSetFromDto(SetUpdateRequestDto setUpdateRequestDto, @MappingTarget Set targetSet);

    @Mapping(source = "notes", target = "numNotes", qualifiedByName = "listNotesToNumNotes")
    @Mapping(source = "id", target = "id")
    SetResponseDto toSetResponseDto(Set set);

    @Named("listNotesToNumNotes")
    default Long numNotesMapping(List<Note> listNotes) {
        return (long) listNotes.size();
    }
}
