package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.model.flashcard.CardItem;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CardItemMapper.class, SetMapper.class})
public interface FlashcardMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Flashcard toFlashcard(FlashcardCreateRequestDto flashcardCreateRequestDto);

    @Mapping(source = "create_method", target = "createMethod")
    @Mapping(source = "cards", target = "numCards", qualifiedByName = "listToCount")
    FlashcardResponseDto toFlashcardResponseDto(Flashcard flashcard);

    @Mapping(source = "create_method", target = "createMethod")
    @Mapping(source = "set", target = "set")
    DetailFlashcardResponseDto toDetailFlashcardResponseDto(Flashcard flashcard);

    @Named("listToCount")
    default Long listToCount(List<CardItem> list) {
        return list == null ? 0L : (long) list.size();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFlashcardFromDto(FlashcardUpdatingRequestDto flashcardUpdatingRequestDto, @MappingTarget Flashcard flashcard);
}
