package com.cabybara.prolearningplatform.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.cabybara.prolearningplatform.dto.request.share.VerifyAccessRequest;
import com.cabybara.prolearningplatform.dto.request.share.YjsStateSaveRequest;
import com.cabybara.prolearningplatform.dto.response.share.VerifyAccessResponse;
import com.cabybara.prolearningplatform.dto.response.share.YjsStateResponse;
import com.cabybara.prolearningplatform.service.note.CollabService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;

import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "Endpoints for collaborative editing features")
public class CollabController {
    private final CollabService collabService;

    private final AuthenticationContext authenticationContext;

    @Value("${internal.service-key}")
    private String serviceSecret;

    @PostMapping("/notes/collab/verify-access")
    public ResponseEntity<ApiResponse<VerifyAccessResponse>> verifyAccess(
            @RequestBody VerifyAccessRequest request
    ) {
        Long userId = authenticationContext.getCurrentUserId();

        VerifyAccessResponse response = collabService.verifyAccess(userId, request.getNoteId());

        return ResponseEntity.status(HttpStatus.OK).body(
            ResponseUtil.success(
                "Access verified",
                response,
                null
            )
        );
    }

    @GetMapping("/internal/notes/{noteId}/yjs-state")
    public ResponseEntity<ApiResponse<YjsStateResponse>> getYjsState(
        @RequestHeader("X-Service-Key") String serviceKey,
        @PathVariable Long noteId
    ) {
        validateServiceKey(serviceKey);

        YjsStateResponse response = collabService.getYjsState(noteId);

        return ResponseEntity.status(HttpStatus.OK).body(
            ResponseUtil.success(
                "Yjs state retrieved",
                response,
                null
            )
        );
    }

    @PutMapping("/internal/notes/{noteId}/yjs-state")
    public ResponseEntity<ApiResponse<Void>> saveYjsState(
        @RequestHeader("X-Service-Key") String serviceKey,
        @PathVariable Long noteId,
        @RequestBody YjsStateSaveRequest request
    ) {
        validateServiceKey(serviceKey);

        collabService.saveYjsState(noteId, request.getYjsState());
        
        return ResponseEntity.status(HttpStatus.OK).body(
            ResponseUtil.success(
                "Yjs state saved",
                null,
                null
            )
        );
    }
    
    private void validateServiceKey(String key) {
        if (!serviceSecret.equals(key)) {
            throw new AccessDeniedException("Invalid service key");
        }
    }
}
