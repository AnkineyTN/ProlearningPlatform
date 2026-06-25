package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.llm.SaveLlmConfigRequestDto;
import com.cabybara.prolearningplatform.dto.response.llm.LlmConfigResponseDto;
import com.cabybara.prolearningplatform.service.llm.UserLlmConfigService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users/me/llm-configs")
@RequiredArgsConstructor
@Tag(name = "User LLM Config (BYOK)")
@PreAuthorize("isAuthenticated()")
public class UserLlmConfigController {

    private final UserLlmConfigService userLlmConfigService;

    @GetMapping
    @Operation(summary = "List my LLM configs", description = "Returns all of the current user's LLM configs with the API key masked")
    public ResponseEntity<ApiResponse<List<LlmConfigResponseDto>>> getMyConfigs() {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get LLM configs successfully", userLlmConfigService.getMyConfigs(), null));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get one LLM config", description = "Returns a single LLM config (with the API key masked) owned by the current user")
    public ResponseEntity<ApiResponse<LlmConfigResponseDto>> getMyConfig(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Get LLM config successfully", userLlmConfigService.getMyConfig(id), null));
    }

    @PostMapping
    @Operation(summary = "Create an LLM config", description = "Creates a new LLM config. The API key is encrypted at rest and never returned. "
            +
            "The first config (or one created with setActive=true) becomes the active config.")
    public ResponseEntity<ApiResponse<LlmConfigResponseDto>> createConfig(
            @Valid @RequestBody SaveLlmConfigRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseUtil.success("Create LLM config successfully", userLlmConfigService.createConfig(request),
                        null));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an LLM config", description = "Updates provider/model/display name; the API key is changed only when supplied")
    public ResponseEntity<ApiResponse<LlmConfigResponseDto>> updateConfig(
            @PathVariable Long id,
            @Valid @RequestBody SaveLlmConfigRequestDto request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Update LLM config successfully",
                        userLlmConfigService.updateConfig(id, request), null));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete an LLM config", description = "Deletes an LLM config owned by the current user")
    public ResponseEntity<ApiResponse<Void>> deleteConfig(@PathVariable Long id) {
        userLlmConfigService.deleteConfig(id);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Delete LLM config successfully", null, null));
    }

    @PutMapping("/{id}/active")
    @Operation(summary = "Set active LLM config", description = "Makes the given config the active one; all other configs of the user are deactivated")
    public ResponseEntity<ApiResponse<LlmConfigResponseDto>> setActive(@PathVariable Long id) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ResponseUtil.success("Set active LLM config successfully", userLlmConfigService.setActive(id),
                        null));
    }
}
