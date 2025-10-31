package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.FlashcardCreateRequestDto;
import com.cabybara.prolearningplatform.dto.response.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.DetailFlashcardResponseDto;
import com.cabybara.prolearningplatform.dto.response.FlashcardResponseDto;
import com.cabybara.prolearningplatform.model.CardItem;
import com.cabybara.prolearningplatform.model.Flashcard;
import org.mapstruct.*;
import org.springframework.boot.actuate.health.NamedContributors;

import java.util.List;

@Mapper(componentModel = "spring",unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FlashcardMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Flashcard toFlashcard(FlashcardCreateRequestDto flashcardCreateRequestDto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    CardItem toCardItem(CardItemCreateRequestDto dto);

    List<CardItem> toCardItemList(List<CardItemCreateRequestDto> dtos);

    @Mapping(source = "create_method", target = "createMethod")
    FlashcardResponseDto toFlashcardResponseDto(Flashcard flashcard);

    @Mapping(source = "create_method", target = "createMethod")
    DetailFlashcardResponseDto toDetailFlashcardResponseDto(Flashcard flashcard);

    @Mapping(source = "image.url", target = "imageUrl")
    CardItemResponseDto toCardItemResponseDto(CardItem cardItem);

    List<CardItemResponseDto> toCardItemResponseDtoList(List<CardItem> cardItems);
}
