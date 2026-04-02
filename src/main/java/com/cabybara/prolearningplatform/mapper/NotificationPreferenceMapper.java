package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.request.notification.UpdateNotificationPreferenceRequestDto;
import com.cabybara.prolearningplatform.dto.response.notification.NotificationPreferenceResponseDto;
import com.cabybara.prolearningplatform.model.noti.NotificationPreference;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationPreferenceMapper {

    NotificationPreferenceResponseDto toResponseDto(NotificationPreference entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(
            UpdateNotificationPreferenceRequestDto dto,
            @MappingTarget NotificationPreference entity);

}