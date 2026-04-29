package com.cabybara.prolearningplatform.service.pomodoro;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;

public interface PomodoroAdminService {
    // Spaces
    SpaceResponseDto createSystemSpace(CreateSystemSpaceRequestDto dto);
    SpaceResponseDto updateSystemSpace(Long spaceId, UpdateSystemSpaceRequestDto dto);
    void deleteSystemSpace(Long spaceId);

    // Sounds
    SoundResponseDto createSystemSound(CreateSystemSoundRequestDto dto);
    SoundResponseDto updateSystemSound(Long soundId, UpdateSystemSoundRequestDto dto);
    void deleteSystemSound(Long soundId);
}