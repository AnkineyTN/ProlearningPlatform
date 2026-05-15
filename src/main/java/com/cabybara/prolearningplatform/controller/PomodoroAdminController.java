package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.pomodoro.AdminCreatePomodoroAssetRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.CreateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSoundRequestDto;
import com.cabybara.prolearningplatform.dto.request.pomodoro.UpdateSystemSpaceRequestDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SoundResponseDto;
import com.cabybara.prolearningplatform.dto.response.pomodoro.SpaceResponseDto;
import com.cabybara.prolearningplatform.service.pomodoro.PomodoroAdminService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/pomodoro")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin — Pomodoro Assets")
@SecurityRequirement(name = "bearerAuth")
public class PomodoroAdminController {

    private final PomodoroAdminService adminService;

    // ── Spaces ───────────────────────────────────────────────────────────────

    @GetMapping("/spaces")
    public ResponseEntity<ApiResponse<List<SpaceResponseDto>>> listSystemSpaces() {
        return ResponseEntity.ok(ResponseUtil.success("OK", adminService.listSystemSpaces(), null));
    }

    @PostMapping("/spaces/url")
    public ResponseEntity<ApiResponse<SpaceResponseDto>> createSystemSpaceFromUrl(
            @RequestBody @Valid AdminCreatePomodoroAssetRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("System space created", adminService.createSystemSpaceFromUrl(dto), null));
    }

    @PostMapping("/spaces")
    public ResponseEntity<ApiResponse<SpaceResponseDto>> createSystemSpace(
            @RequestBody @Valid CreateSystemSpaceRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("System space created", adminService.createSystemSpace(dto), null));
    }

    @PutMapping("/spaces/{spaceId}")
    public ResponseEntity<ApiResponse<SpaceResponseDto>> updateSystemSpace(
            @PathVariable Long spaceId,
            @RequestBody @Valid UpdateSystemSpaceRequestDto dto) {
        return ResponseEntity.ok(ResponseUtil.success("System space updated", adminService.updateSystemSpace(spaceId, dto), null));
    }

    @DeleteMapping("/spaces/{spaceId}")
    public ResponseEntity<ApiResponse<Void>> deleteSystemSpace(@PathVariable Long spaceId) {
        adminService.deleteSystemSpace(spaceId);
        return ResponseEntity.ok(ResponseUtil.success("System space deleted", null, null));
    }

    // ── Sounds ───────────────────────────────────────────────────────────────

    @GetMapping("/sounds")
    public ResponseEntity<ApiResponse<List<SoundResponseDto>>> listSystemSounds() {
        return ResponseEntity.ok(ResponseUtil.success("OK", adminService.listSystemSounds(), null));
    }

    @PostMapping("/sounds/url")
    public ResponseEntity<ApiResponse<SoundResponseDto>> createSystemSoundFromUrl(
            @RequestBody @Valid AdminCreatePomodoroAssetRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("System sound created", adminService.createSystemSoundFromUrl(dto), null));
    }

    @PostMapping("/sounds")
    public ResponseEntity<ApiResponse<SoundResponseDto>> createSystemSound(
            @RequestBody @Valid CreateSystemSoundRequestDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseUtil.success("System sound created", adminService.createSystemSound(dto), null));
    }

    @PutMapping("/sounds/{soundId}")
    public ResponseEntity<ApiResponse<SoundResponseDto>> updateSystemSound(
            @PathVariable Long soundId,
            @RequestBody @Valid UpdateSystemSoundRequestDto dto) {
        return ResponseEntity.ok(ResponseUtil.success("System sound updated", adminService.updateSystemSound(soundId, dto), null));
    }

    @DeleteMapping("/sounds/{soundId}")
    public ResponseEntity<ApiResponse<Void>> deleteSystemSound(@PathVariable Long soundId) {
        adminService.deleteSystemSound(soundId);
        return ResponseEntity.ok(ResponseUtil.success("System sound deleted", null, null));
    }
}
