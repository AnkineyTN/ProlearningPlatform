package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.response.llm.LlmConfigResponseDto;
import com.cabybara.prolearningplatform.model.llm.UserLlmConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LlmConfigMapper {

    @Mapping(target = "provider",
            expression = "java(config.getProvider() == null ? null : config.getProvider().getWireValue())")
    @Mapping(target = "apiKeyMasked",
            expression = "java(\"****\" + (config.getApiKeyLast4() == null ? \"\" : config.getApiKeyLast4()))")
    LlmConfigResponseDto toResponseDto(UserLlmConfig config);
}
