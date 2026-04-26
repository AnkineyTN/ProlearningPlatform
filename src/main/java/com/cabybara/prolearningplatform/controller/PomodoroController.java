package com.cabybara.prolearningplatform.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.cabybara.prolearningplatform.dto.request.pomodoro.PomodoroSettingRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.PomodoroSettingResponseDto;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroSettingService;
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
}
