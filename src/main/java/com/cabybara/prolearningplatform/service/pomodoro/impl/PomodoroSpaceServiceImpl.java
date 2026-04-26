package com.cabybara.prolearningplatform.service.pomodoro.impl;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;
import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.exception.ResourceNotFoundException;
import com.cabybara.prolearningplatform.model.Asset;
import com.cabybara.prolearningplatform.model.User;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroSpace;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserActiveSpace;
import com.cabybara.prolearningplatform.model.pomodoro.PomodoroUserFavoriteSpace;
import com.cabybara.prolearningplatform.repository.AssetRepository;
import com.cabybara.prolearningplatform.repository.PomodoroSpaceRepository;
import com.cabybara.prolearningplatform.repository.PomodoroUserActiveSpaceRepository;
import com.cabybara.prolearningplatform.repository.PomodoroUserFavoriteSpaceRepository;
import com.cabybara.prolearningplatform.service.asset.AssetService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSpaceService;
import com.cabybara.prolearningplatform.service.user.UserService;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PomodoroSpaceServiceImpl implements PomodoroSpaceService {
    private final PomodoroSpaceRepository spaceRepository;
    private final PomodoroUserActiveSpaceRepository activeSpaceRepository;
    private final PomodoroUserFavoriteSpaceRepository favoriteSpaceRepository;
    private final AssetService assetService;
    private final AuthenticationContext authenticationContext;
    private final UserService userService;

    @Override
    public List<SpaceResponseDto> getAllSpaces() {
        Long userId = authenticationContext.getCurrentUserId();
        List<PomodoroSpace> spaces = spaceRepository.findAllAvailableForUser(userId);

        Set<Long> favoriteIds = favoriteSpaceRepository.findFavoriteSpaceIdsByUserId(userId);
        Long activeSpaceId = activeSpaceRepository.findByUserId(userId)
                .map(a -> a.getSpace().getId())
                .orElse(null);

        return spaces.stream()
                .map(s -> toDto(s, favoriteIds.contains(s.getId()),
                                   s.getId().equals(activeSpaceId)))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SpaceResponseDto createUserSpace(CreateSpaceRequestDto dto) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);
        Asset asset = assetService.findAndActivateAsset(dto.getAssetId(), userId);

        PomodoroSpace space = PomodoroSpace.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .source(AssetSource.USER)
                .asset(asset)
                .user(user)
                .build();

        space = spaceRepository.save(space);
        return toDto(space, false, false);
    }

    @Override
    @Transactional
    public void deleteUserSpace(Long spaceId) {
        Long userId = authenticationContext.getCurrentUserId();
        PomodoroSpace space = spaceRepository.findByIdAndUserId(spaceId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Space not found: " + spaceId));

        // Đánh dấu asset là deleted
        assetService.markDeletedAsset(space.getAsset());
        // Soft delete space
        space.setIsActive(false);
        spaceRepository.save(space);
    }

    @Override
    @Transactional
    public void setActiveSpace(Long spaceId) {
        Long userId = authenticationContext.getCurrentUserId();
        User user = userService.getUserById(userId);

        PomodoroSpace space = spaceRepository.findById(spaceId)
                .filter(s -> s.getIsActive() && (
                        s.getSource() == AssetSource.SYSTEM ||
                        s.getUser().getId().equals(userId)))
                .orElseThrow(() -> new ResourceNotFoundException("Space not found: " + spaceId));

        // Xóa active cũ, set active mới
        activeSpaceRepository.deleteByUserId(userId);
        activeSpaceRepository.save(PomodoroUserActiveSpace.builder().user(user).space(space).build());
    }

    @Override       
    @Transactional
    public void toggleFavoriteSpace(Long spaceId) {
        Long userId = authenticationContext.getCurrentUserId();

        if (favoriteSpaceRepository.existsByUserIdAndSpaceId(userId, spaceId)) {
            favoriteSpaceRepository.deleteByUserIdAndSpaceId(userId, spaceId);
        } else {
            User user = userService.getUserById(userId);
            PomodoroSpace space = spaceRepository.findById(spaceId)
                    .orElseThrow(() -> new ResourceNotFoundException("Space not found: " + spaceId));
            favoriteSpaceRepository.save(PomodoroUserFavoriteSpace.builder().user(user).space(space).build());
        }
    }

    private SpaceResponseDto toDto(PomodoroSpace s, boolean isFavorite, boolean isActive) {
        return SpaceResponseDto.builder()
                .id(s.getId())
                .name(s.getName())
                .description(s.getDescription())
                .assetUrl(s.getAsset().getUrl())
                .assetType(s.getAsset().getType())
                .source(s.getSource())
                .isFavorite(isFavorite)
                .isActive(isActive)
                .build();
    }
}
