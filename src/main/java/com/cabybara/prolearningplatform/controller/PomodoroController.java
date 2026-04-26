package com.cabybara.prolearningplatform.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.LogSessionRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSettingRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSoundSearchRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSpaceSearchRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.SetActiveSoundRequestDto;
import com.cabybara.prolearningplatform.dto.response.PaginationResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.PomodoroSettingResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.WeeklyStatsResponseDto;
import com.cabybara.prolearningplatform.enums.AssetSource;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSessionService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSettingService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSoundService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSpaceService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/pomodoro")
@Tag(name = "Pomodoro")
@RequiredArgsConstructor
public class PomodoroController {
    private final PomodoroSettingService settingService;
    private final PomodoroSpaceService spaceService;
    private final PomodoroSoundService soundService;
    private final PomodoroSessionService sessionService;

    @GetMapping("/setting")
    public ResponseEntity<ApiResponse<PomodoroSettingResponseDto>> getSetting() {
        return ResponseEntity.ok(
            ResponseUtil.success("Pomodoro setting retrieved successfully", settingService.getSetting(), null)
        );
    }

    @PutMapping("/setting")
    public ResponseEntity<ApiResponse<PomodoroSettingResponseDto>> updateSetting(
            @RequestBody @Valid PomodoroSettingRequestDto dto) {
        return ResponseEntity.ok(
            ResponseUtil.success("Pomodoro setting updated successfully", settingService.updateSetting(dto), null)
        );
    }

    @GetMapping("/spaces")
    public ResponseEntity<ApiResponse<List<SpaceResponseDto>>> getAllSpaces() {
        return ResponseEntity.ok(
            ResponseUtil.success("Spaces retrieved successfully", spaceService.getAllSpaces(), null)
        );
    }

    @GetMapping("/spaces/search")
    public ResponseEntity<ApiResponse<List<SpaceResponseDto>>> searchSpaces(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AssetSource source,
            @RequestParam(defaultValue = "ALL") PomodoroSpaceSearchRequestDto.SpaceTab tab,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PomodoroSpaceSearchRequestDto request = new PomodoroSpaceSearchRequestDto();
            request.setKeyword(keyword);
            request.setSource(source);
            request.setTab(tab);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);

        Page<SpaceResponseDto> resultPage = spaceService.searchSpaces(request);

        PaginationResponseDto pagination = PaginationResponseDto.builder()
            .currentPage(resultPage.getNumber())
            .totalPages(resultPage.getTotalPages())
            .totalItems(resultPage.getTotalElements())
            .pageSize(resultPage.getSize())
            .build();

        return ResponseEntity.ok(
            ResponseUtil.success(
                "Spaces retrieved successfully",
                resultPage.getContent(),
                pagination
            )
        );
    }

    @PostMapping("/spaces")
    public ResponseEntity<ApiResponse<SpaceResponseDto>> createSpace(
            @RequestBody @Valid CreateSpaceRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ResponseUtil.success("Space created successfully", spaceService.createUserSpace(dto), null)
        );
    }

    @DeleteMapping("/spaces/{spaceId}")
    public ResponseEntity<ApiResponse<Void>> deleteSpace(@PathVariable Long spaceId) {
        spaceService.deleteUserSpace(spaceId);
        return ResponseEntity.ok(
            ResponseUtil.success("Space deleted successfully", null, null)
        );
    }

    @PutMapping("/spaces/{spaceId}/activate")
    public ResponseEntity<ApiResponse<Void>> setActiveSpace(@PathVariable Long spaceId) {
        spaceService.setActiveSpace(spaceId);
        return ResponseEntity.ok(
            ResponseUtil.success("Space activated successfully", null, null)
        );
    }

    @PostMapping("/spaces/{spaceId}/favorite")
    public ResponseEntity<ApiResponse<Void>> toggleFavoriteSpace(@PathVariable Long spaceId) {
        spaceService.toggleFavoriteSpace(spaceId);
        return ResponseEntity.ok(
            ResponseUtil.success("Favorite status updated successfully", null, null)
        );
    }

    @GetMapping("/sounds")
    public ResponseEntity<ApiResponse<List<SoundResponseDto>>> getAllSounds() {
        return ResponseEntity.ok(
            ResponseUtil.success("Sounds retrieved successfully", soundService.getAllSounds(), null)
        );
    }

    @GetMapping("/sounds/search")
    public ResponseEntity<ApiResponse<List<SoundResponseDto>>> searchSounds(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) AssetSource source,
            @RequestParam(defaultValue = "ALL") PomodoroSoundSearchRequestDto.SoundTab tab,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(50) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        PomodoroSoundSearchRequestDto request = new PomodoroSoundSearchRequestDto();
            request.setKeyword(keyword);
            request.setSource(source);
            request.setTab(tab);
            request.setPage(page);
            request.setSize(size);
            request.setSortBy(sortBy);
            request.setSortDir(sortDir);

        Page<SoundResponseDto> resultPage = soundService.searchSounds(request);

        PaginationResponseDto pagination = PaginationResponseDto.builder()
            .currentPage(resultPage.getNumber())
            .totalPages(resultPage.getTotalPages())
            .totalItems(resultPage.getTotalElements())
            .pageSize(resultPage.getSize())
            .build();

        return ResponseEntity.ok(
            ResponseUtil.success("Sounds retrieved successfully", resultPage.getContent(), pagination)
        );
    }

    @PostMapping("/sounds")
    public ResponseEntity<ApiResponse<SoundResponseDto>> createSound(
            @RequestBody @Valid CreateSoundRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ResponseUtil.success("Sound created successfully", soundService.createUserSound(dto), null)
        );
    }

    @DeleteMapping("/sounds/{soundId}")
    public ResponseEntity<ApiResponse<Void>> deleteSound(@PathVariable Long soundId) {
        soundService.deleteUserSound(soundId);
        return ResponseEntity.ok(
            ResponseUtil.success("Sound deleted successfully", null, null)
        );
    }

    @PostMapping("/sounds/active")
    public ResponseEntity<ApiResponse<Void>> addActiveSound(@RequestBody @Valid SetActiveSoundRequestDto dto) {
        soundService.addActiveSound(dto);
        return ResponseEntity.ok(
            ResponseUtil.success("Active sound added successfully", null, null)
        );
    }

    @DeleteMapping("/sounds/active/{soundId}")
    public ResponseEntity<ApiResponse<Void>> removeActiveSound(@PathVariable Long soundId) {
        soundService.removeActiveSound(soundId);
        return ResponseEntity.ok(
            ResponseUtil.success("Active sound removed successfully", null, null)
        );
    }

    @DeleteMapping("/sounds/active")
    public ResponseEntity<ApiResponse<Void>> clearAllActiveSounds() {
        soundService.clearAllActiveSounds();
        return ResponseEntity.ok(
            ResponseUtil.success("All active sounds cleared successfully", null, null)
        );
    }

    @PostMapping("/sounds/{soundId}/favorite")
    public ResponseEntity<ApiResponse<Void>> toggleFavoriteSound(@PathVariable Long soundId) {
        soundService.toggleFavoriteSound(soundId);
        return ResponseEntity.ok(
            ResponseUtil.success("Favorite status updated successfully", null, null)
        );
    }

    @PostMapping("/sessions")
    public ResponseEntity<ApiResponse<Void>> logSession(@RequestBody @Valid LogSessionRequestDto dto) {
        sessionService.logSession(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(
            ResponseUtil.success("Session logged successfully", null, null)
        );
    }

    @GetMapping("/stats/weekly")
    public ResponseEntity<ApiResponse<WeeklyStatsResponseDto>> getWeeklyStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "UTC") String timezone) {
        return ResponseEntity.ok(
            ResponseUtil.success("Weekly stats retrieved successfully", sessionService.getWeeklyStats(startDate, timezone), null)
        );
    }
}
