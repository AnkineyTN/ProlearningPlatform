package com.cabybara.prolearningplatform.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cabybara.prolearningplatform.dto.request.share.UpdateMemberRoleRequest;
import com.cabybara.prolearningplatform.dto.request.share.VerifyAccessRequest;
import com.cabybara.prolearningplatform.dto.request.share.YjsStateSaveRequest;
import com.cabybara.prolearningplatform.dto.response.note.AcceptByTokenResponse;
import com.cabybara.prolearningplatform.dto.response.share.PendingInviteResponse;
import com.cabybara.prolearningplatform.dto.response.share.VerifyAccessResponse;
import com.cabybara.prolearningplatform.dto.response.share.YjsStateResponse;
import com.cabybara.prolearningplatform.service.note.CollabService;
import com.cabybara.prolearningplatform.service.note.NoteService;
import com.cabybara.prolearningplatform.service.flashcard.FlashcardService;
import com.cabybara.prolearningplatform.service.exam.ExamService;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import com.cabybara.prolearningplatform.utils.ApiResponse;
import com.cabybara.prolearningplatform.utils.AuthenticationContext;
import com.cabybara.prolearningplatform.utils.ResponseUtil;

import org.springframework.web.bind.annotation.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequiredArgsConstructor
@Tag(name = "Collaboration", description = "Endpoints for collaborative editing features")
public class CollabController {
    private final CollabService collabService;
    private final NoteService noteService;
    private final FlashcardService flashcardService;
    private final ExamService examService;
    private final NotePermissionService notePermissionService;

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

    @PostMapping("/notes/invites/accept-by-token")
    public ResponseEntity<ApiResponse<AcceptByTokenResponse>> acceptByToken(
        @RequestParam String token
    ) {
        AcceptByTokenResponse response = noteService.acceptByToken(token);
        
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.GONE)
            .body(ResponseUtil.success(
                response.isSuccess() ? "Invite accepted successfully" : "Invite acceptance failed",
                response,
                null
            ));
    }
    
     
    @GetMapping("/notes/invites/pending")
    public ResponseEntity<ApiResponse<List<PendingInviteResponse>>> getPendingInvites(
    ) {
        List<PendingInviteResponse> pendingInvites = noteService.getPendingInvites();
        return ResponseEntity.ok(
            ResponseUtil.success("Pending invites retrieved successfully", pendingInvites, null)
        );
    }

    @PostMapping("/flashcard-invites/accept-by-token")
    public ResponseEntity<ApiResponse<AcceptByTokenResponse>> acceptFlashcardByToken(
        @RequestParam String token
    ) {
        AcceptByTokenResponse response = flashcardService.acceptByToken(token);
        
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.GONE)
            .body(ResponseUtil.success(
                response.isSuccess() ? "Invite accepted successfully" : "Invite acceptance failed",
                response,
                null
            ));
    }

    @GetMapping("/flashcards/pending")
    public ResponseEntity<ApiResponse<List<PendingInviteResponse>>> getPendingFlashcardInvites(
    ) {
        List<PendingInviteResponse> pendingInvites = flashcardService.getPendingInvites();
        return ResponseEntity.ok(
            ResponseUtil.success("Pending invites retrieved successfully", pendingInvites, null)
        );
    }

    @PostMapping("/exam-invites/accept-by-token")
    public ResponseEntity<ApiResponse<AcceptByTokenResponse>> acceptExamByToken(
        @RequestParam String token
    ) {
        AcceptByTokenResponse response = examService.acceptByToken(token);
        
        return ResponseEntity.status(response.isSuccess() ? HttpStatus.OK : HttpStatus.GONE)
            .body(ResponseUtil.success(
                response.isSuccess() ? "Invite accepted successfully" : "Invite acceptance failed",
                response,
                null
            ));
    }

    @GetMapping("/exams/pending")
    public ResponseEntity<ApiResponse<List<PendingInviteResponse>>> getPendingExamInvites(
    ) {
        List<PendingInviteResponse> pendingInvites = examService.getPendingInvites();
        return ResponseEntity.ok(
            ResponseUtil.success("Pending invites retrieved successfully", pendingInvites, null)
        );
    }

    @PatchMapping("/notes/{noteId}/members/{targetUserId}/role")
    @PreAuthorize("@notePermissionService.isOwner(@authenticationContext.getCurrentUserId(), #noteId)")
    public ResponseEntity<ApiResponse<Void>> updateMemberRole(
            @PathVariable Long noteId,
            @PathVariable Long targetUserId,
            @RequestBody UpdateMemberRoleRequest request
    ) {
        notePermissionService.updateMemberRole(
            noteId, targetUserId, request.getRole(),
            authenticationContext.getCurrentUserId()
        );

        return ResponseEntity.status(HttpStatus.OK).body(
            ResponseUtil.success("Member role updated successfully", null, request)
        );
    }

    private void validateServiceKey(String key) {
        if (!serviceSecret.equals(key)) {
            throw new AccessDeniedException("Invalid service key");
        }
    }
}
