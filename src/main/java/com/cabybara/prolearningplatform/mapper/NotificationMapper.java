package com.cabybara.prolearningplatform.mapper;

import com.cabybara.prolearningplatform.dto.response.notification.NotificationResponseDto;
import com.cabybara.prolearningplatform.model.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {

    NotificationResponseDto toResponseDto(Notification notification);

    List<NotificationResponseDto> toResponseDtoList(List<Notification> notifications);
}

