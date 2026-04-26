package com.cabybara.prolearningplatform.service.pomodoro.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;
import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.enums.AssetStatus;
import com.cabybara.prolearningplatform.enums.AssetType;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSound;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSpace;
import com.cabybara.prolearningplatform.repository.AssetRepository;
import com.cabybara.prolearningplatform.repository.PomodoroSoundRepository;
import com.cabybara.prolearningplatform.repository.PomodoroSpaceRepository;
import com.cabybara.prolearningplatform.service.cloudinary.CloudinaryService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroAdminService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroAdminServiceImpl implements PomodoroAdminService {
 private final PomodoroSpaceRepository spaceRepository;
    private final PomodoroSoundRepository soundRepository;
    private final AssetRepository assetRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional
    public SpaceResponseDto createSystemSpace(CreateSystemSpaceRequestDto dto) {
        Asset asset = Asset.builder()
                .publicId(dto.getPublicId())
                .url(dto.getUrl())
                .type(dto.getAssetType())   
                .status(AssetStatus.ACTIVE)
                .fileName(extractFileName(dto.getUrl()))
                .user(null)           
                .build();
        asset = assetRepository.save(asset);

        PomodoroSpace space = PomodoroSpace.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .source("SYSTEM")
                .asset(asset)
                .user(null)         
                .build();

        space = spaceRepository.save(space);
        return toSpaceDto(space);
    }

    @Override
    @Transactional
    public SpaceResponseDto updateSystemSpace(Long spaceId, UpdateSystemSpaceRequestDto dto) {
        PomodoroSpace space = spaceRepository.findById(spaceId)
                .filter(s -> "SYSTEM".equals(s.getSource()))
                .orElseThrow(() -> new ResourceNotFoundException("System space not found: " + spaceId));

        space.setName(dto.getName());
        space.setDescription(dto.getDescription());

        if (dto.getPublicId() != null && dto.getUrl() != null) {
            Asset oldAsset = space.getAsset();
            oldAsset.setStatus(AssetStatus.DELETED);
            assetRepository.save(oldAsset);

            Asset newAsset = Asset.builder()
                    .publicId(dto.getPublicId())
                    .url(dto.getUrl())
                    .type(dto.getAssetType() != null ? dto.getAssetType() : oldAsset.getType())
                    .status(AssetStatus.ACTIVE)
                    .fileName(extractFileName(dto.getUrl()))
                    .user(null)
                    .build();
            newAsset = assetRepository.save(newAsset);
            space.setAsset(newAsset);
        }

        return toSpaceDto(spaceRepository.save(space));
    }

    @Override
    @Transactional
    public void deleteSystemSpace(Long spaceId) {
        PomodoroSpace space = spaceRepository.findById(spaceId)
                .filter(s -> "SYSTEM".equals(s.getSource()))
                .orElseThrow(() -> new ResourceNotFoundException("System space not found: " + spaceId));

        space.getAsset().setStatus(AssetStatus.DELETED);
        assetRepository.save(space.getAsset());

        space.setIsActive(false);
        spaceRepository.save(space);
    }

    @Override
    @Transactional
    public SoundResponseDto createSystemSound(CreateSystemSoundRequestDto dto) {
        Asset asset = Asset.builder()
                .publicId(dto.getPublicId())
                .url(dto.getUrl())
                .type(AssetType.AUDIO)
                .status(AssetStatus.ACTIVE)
                .fileName(extractFileName(dto.getUrl()))
                .user(null)
                .build();
        asset = assetRepository.save(asset);

        PomodoroSound sound = PomodoroSound.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .source("SYSTEM")
                .asset(asset)
                .user(null)
                .build();

        return toSoundDto(soundRepository.save(sound));
    }

    @Override
    @Transactional
    public SoundResponseDto updateSystemSound(Long soundId, UpdateSystemSoundRequestDto dto) {
        PomodoroSound sound = soundRepository.findById(soundId)
                .filter(s -> "SYSTEM".equals(s.getSource()))
                .orElseThrow(() -> new ResourceNotFoundException("System sound not found: " + soundId));

        sound.setName(dto.getName());
        sound.setDescription(dto.getDescription());

        if (dto.getPublicId() != null && dto.getUrl() != null) {
            Asset oldAsset = sound.getAsset();
            oldAsset.setStatus(AssetStatus.DELETED);
            assetRepository.save(oldAsset);

            Asset newAsset = Asset.builder()
                    .publicId(dto.getPublicId())
                    .url(dto.getUrl())
                    .type(AssetType.AUDIO)
                    .status(AssetStatus.ACTIVE)
                    .fileName(extractFileName(dto.getUrl()))
                    .user(null)
                    .build();
            sound.setAsset(assetRepository.save(newAsset));
        }

        return toSoundDto(soundRepository.save(sound));
    }

    @Override
    @Transactional
    public void deleteSystemSound(Long soundId) {
        PomodoroSound sound = soundRepository.findById(soundId)
                .filter(s -> "SYSTEM".equals(s.getSource()))
                .orElseThrow(() -> new ResourceNotFoundException("System sound not found: " + soundId));

        sound.getAsset().setStatus(AssetStatus.DELETED);
        assetRepository.save(sound.getAsset());
        sound.setIsActive(false);
        soundRepository.save(sound);
    }

    private String extractFileName(String url) {
        // Lấy tên file từ URL Cloudinary, VD:
        // https://res.cloudinary.com/demo/video/upload/v123/ProLearning/rain.mp4 → rain.mp4
        if (url == null) return "";
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private SpaceResponseDto toSpaceDto(PomodoroSpace s) {
        return SpaceResponseDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .assetUrl(s.getAsset().getUrl())
                .assetType(s.getAsset().getType())
                .source(AssetSource.valueOf(s.getSource()))
                .isFavorite(false)
                .isActive(false)
                .build();
    }

    private SoundResponseDto toSoundDto(PomodoroSound s) {
        return SoundResponseDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .assetUrl(s.getAsset().getUrl())
                .source(AssetSource.valueOf(s.getSource()))
                .isFavorite(false)
                .isActive(false)
                .volume(null)
                .build();
    }
}