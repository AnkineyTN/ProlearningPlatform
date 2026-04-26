package com.cabybara.prolearningplatform.service.pomodoro.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.SetActiveSoundRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;
import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSound;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSpace;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserActiveSound;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserFavoriteSound;
import com.cabybara.prolearningplatform.repository.PomodoroSoundRepository;
import com.cabybara.prolearningplatform.repository.PomodoroUserActiveSoundRepository;
import com.cabybara.prolearningplatform.repository.PomodoroUserFavoriteSoundRepository;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSoundService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroSoundServiceImpl implements PomodoroSoundService {
    private final PomodoroSoundRepository soundRepository;
    private final PomodoroUserActiveSoundRepository activeSoundRepository;
    private final PomodoroUserFavoriteSoundRepository favoriteSoundRepository;
    private final AssetService assetService;
    private final AuthenticationContext authenticationContext;
    private final UserService userService;

    @Override
    public List<SoundResponseDto> getAllSounds() {
        Long userId = authenticationContext.getCurrentUserId();
        List<PomodoroSound> sounds = soundRepository.findAllAvailableForUser(userId);

        Set<Long> favoriteIds = favoriteSoundRepository.findFavoriteSoundIdsByUserId(userId);
        // Map soundId -> volume
        Map<Long, Float> activeMap = activeSoundRepository.findByUserId(userId).stream()
                .collect(Collectors.toMap(a -> a.getSound().getId(), PomodoroUserActiveSound::getVolume));

        return sounds.stream()
                .map(s -> toDto(s, favoriteIds.contains(s.getId()), activeMap.get(s.getId())))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SoundResponseDto createUserSound(CreateSoundRequestDto dto) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);
        Asset asset = assetService.findAndActivateAsset(dto.getAssetId(), userId);

        PomodoroSound sound = PomodoroSound.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .source(AssetSource.USER)
                .asset(asset)
                .user(user)
                .build();

        sound = soundRepository.save(sound);

        return toDto(sound, false, null);
    }

    @Override
    @Transactional
    public void deleteUserSound(Long soundId) {
        Long userId = authenticationContext.getCurrentUserId();
        PomodoroSound sound = soundRepository.findById(soundId)
                .orElseThrow(() -> new ResourceNotFoundException("Sound not found: " + soundId));
        if (!sound.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Sound not found: " + soundId);
        }

        // Đánh dấu asset là deleted
        assetService.markDeletedAsset(sound.getAsset());
        // Soft delete sound
        sound.setIsActive(false);
        soundRepository.save(sound);
    }

    @Override
    @Transactional
    public void addActiveSound(SetActiveSoundRequestDto dto) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);

        Optional<PomodoroUserActiveSound> existing = activeSoundRepository
                .findByUserIdAndSoundId(userId, dto.getSoundId());

        if (existing.isPresent()) {
            // Chỉ update volume
            existing.get().setVolume(dto.getVolume());
            activeSoundRepository.save(existing.get());
        } else {
            PomodoroSound sound = soundRepository.findById(dto.getSoundId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sound not found: " + dto.getSoundId()));
            activeSoundRepository.save(PomodoroUserActiveSound.builder()
                    .user(user).sound(sound).volume(dto.getVolume()).build());
        }
    }

    @Override
    @Transactional
    public void removeActiveSound(Long soundId) {
        Long userId = authenticationContext.getCurrentUserId();
        activeSoundRepository.deleteByUserIdAndSoundId(userId, soundId);
    }

    @Override
    @Transactional
    public void clearAllActiveSounds() {
        Long userId = authenticationContext.getCurrentUserId();
        activeSoundRepository.deleteAllByUserId(userId);
    }

    @Override
    @Transactional
    public void toggleFavoriteSound(Long soundId) {
        Long userId = authenticationContext.getCurrentUserId();

        if (favoriteSoundRepository.existsByUserIdAndSoundId(userId, soundId)) {
            favoriteSoundRepository.deleteByUserIdAndSoundId(userId, soundId);
        } else {
            User user = userService.getUserById(userId);
            PomodoroSound sound = soundRepository.findById(soundId)
                    .orElseThrow(() -> new ResourceNotFoundException("Sound not found: " + soundId));
            favoriteSoundRepository.save(PomodoroUserFavoriteSound.builder().user(user).sound(sound).build());
        }
    }

    private SoundResponseDto toDto(PomodoroSound s, boolean isFavorite, Float volume) {
        return SoundResponseDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .assetUrl(s.getAsset().getUrl())
                .source(s.getSource())
                .isFavorite(isFavorite)
                .isActive(volume != null)
                .volume(volume)
                .build();
    }
}
