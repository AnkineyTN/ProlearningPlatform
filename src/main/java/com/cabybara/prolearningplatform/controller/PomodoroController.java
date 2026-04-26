package com.cabybara.prolearningplatform.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSettingRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.PomodoroSettingResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSettingService;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSpaceService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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

}
