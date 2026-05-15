package com.cabybara.prolearningplatform.service.pomodoro;

import com.cabybara.prolearningplatform.dto.request.pomodoro.AdminCreatePomodoroAssetRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;

import java.util.List;

public interface PomodoroAdminService {
    List<SpaceResponseDto> listSystemSpaces();
    List<SoundResponseDto> listSystemSounds();

    SpaceResponseDto createSystemSpaceFromUrl(AdminCreatePomodoroAssetRequestDto dto);
    SoundResponseDto createSystemSoundFromUrl(AdminCreatePomodoroAssetRequestDto dto);

    SpaceResponseDto createSystemSpace(CreateSystemSpaceRequestDto dto);
    SpaceResponseDto updateSystemSpace(Long spaceId, UpdateSystemSpaceRequestDto dto);
    void deleteSystemSpace(Long spaceId);

    SoundResponseDto createSystemSound(CreateSystemSoundRequestDto dto);
    SoundResponseDto updateSystemSound(Long soundId, UpdateSystemSoundRequestDto dto);
    void deleteSystemSound(Long soundId);
}
