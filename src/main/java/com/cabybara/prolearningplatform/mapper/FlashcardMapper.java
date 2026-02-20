package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.FlashcardUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.FlashcardResponseDto;
import com.cabybara.prolearningplatform.model.flashcard.Flashcard;
import org.mapstruct.*;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {CardItemMapper.class})
public interface FlashcardMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Flashcard toFlashcard(FlashcardCreateRequestDto flashcardCreateRequestDto);

    @Mapping(source = "create_method", target = "createMethod")
    @Mapping(source = "flashcard", target = "numCards", qualifiedByName = "calNumCard")
    FlashcardResponseDto toFlashcardResponseDto(Flashcard flashcard);

    @Mapping(source = "create_method", target = "createMethod")
    DetailFlashcardResponseDto toDetailFlashcardResponseDto(Flashcard flashcard);


    @Named("calNumCard")
    default Long calNumCard(Flashcard flashcard) {
        return (long) flashcard.getCards().size();
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFlashcardFromDto(FlashcardUpdatingRequestDto flashcardUpdatingRequestDto, @MappingTarget Flashcard flashcard);
}
