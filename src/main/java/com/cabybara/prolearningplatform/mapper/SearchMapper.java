package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.helper.SearchResultDto;
import com.cabybara.prolearningplatform.dto.response.search.SearchResponseDto;
import com.cabybara.prolearningplatform.model.SearchIndex;
import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SearchMapper {
    SearchResponseDto toSearchResponseDto(SearchResultDto searchResultDto);
}
