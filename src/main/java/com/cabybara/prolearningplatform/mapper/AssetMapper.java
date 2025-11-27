package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.helper.AssetToDeleteDto;
import com.cabybara.prolearningplatform.model.Asset;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface AssetMapper {
    AssetToDeleteDto toAssetToDeleteDto(Asset asset);
}
