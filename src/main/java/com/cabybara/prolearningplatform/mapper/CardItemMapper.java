package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemCreateRequestDto;
import com.cabybara.prolearningplatform.dto.request.flashcard.CardItemUpdatingRequestDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.CardItemResponseDto;
import com.cabybara.prolearningplatform.dto.response.flashcard.CardLearnResponseDto;
import com.cabybara.prolearningplatform.model.CardItem;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CardItemMapper {
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "nextReviewAt", expression = "java(java.time.OffsetDateTime.now())")
    CardItem toCardItem(CardItemCreateRequestDto dto);

    List<CardItem> toCardItemList(List<CardItemCreateRequestDto> dtos);

    @Mapping(source = "image.url", target = "imageUrl")
    CardItemResponseDto toCardItemResponseDto(CardItem cardItem);

    List<CardItemResponseDto> toCardItemResponseDtoList(List<CardItem> cardItems);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(source = "id", target = "id", ignore = true)
    void updateCardFromDto(CardItemUpdatingRequestDto cardItemUpdatingRequestDto, @MappingTarget CardItem cardItem);

    CardLearnResponseDto toCardLearnResponseDto(CardItem cardItem);
}
