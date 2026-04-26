package com.cabybara.prolearningplatform.service.pomodoro;

import java.util.List;

import org.springframework.data.domain.Page;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSoundSearchRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.SetActiveSoundRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;

public interface PomodoroSoundService {
    List<SoundResponseDto> getAllSounds();
    SoundResponseDto createUserSound(CreateSoundRequestDto dto);
    void deleteUserSound(Long soundId);
    void addActiveSound(SetActiveSoundRequestDto dto);
    void removeActiveSound(Long soundId);
    void clearAllActiveSounds();
    void toggleFavoriteSound(Long soundId);
    Page<SoundResponseDto> searchSounds(PomodoroSoundSearchRequestDto request);
}