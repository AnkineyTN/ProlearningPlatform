package com.cabybara.prolearningplatform.service.pomodoro;

import java.util.List;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;

public interface PomodoroSpaceService {
    List<SpaceResponseDto> getAllSpaces();
    SpaceResponseDto createUserSpace(CreateSpaceRequestDto dto);
    void deleteUserSpace(Long spaceId);
    void setActiveSpace(Long spaceId);
    void toggleFavoriteSpace(Long spaceId);
}