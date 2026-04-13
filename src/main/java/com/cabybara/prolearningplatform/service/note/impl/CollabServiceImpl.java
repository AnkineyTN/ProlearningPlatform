package com.cabybara.prolearningplatform.service.note.impl;


import java.util.Base64;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cabybara.prolearningplatform.dto.response.share.VerifyAccessResponse;
import com.cabybara.prolearningplatform.dto.response.share.YjsStateResponse;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.service.note.CollabService;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollabServiceImpl implements CollabService {
    private final NotePermissionService notePermissionService;
    private final NoteRepository noteRepository;

    @Override
    public VerifyAccessResponse verifyAccess(Long userId, Long noteId) {
        if (!notePermissionService.hasAccess(userId, noteId)) {
            throw new AccessDeniedException(
                "User " + userId + " has no access to note " + noteId);
        }

        String role = notePermissionService.getRole(userId, noteId);

        log.info("[collab] verified user={} role={} note={}", userId, role, noteId);

        return new VerifyAccessResponse(userId, role);
    }

    @Override
    public YjsStateResponse getYjsState(Long noteId) {
        byte[] state = noteRepository.findYjsStateById(noteId).orElse(null);

        if (state == null || state.length == 0) {
            log.info("[collab] No Yjs state for note={} (new note)", noteId);
            return new YjsStateResponse(null);
        }

        return new YjsStateResponse(Base64.getEncoder().encodeToString(state));
    }

    @Override
    @Transactional
    public void saveYjsState(Long noteId, String base64State) {
        byte[] state = Base64.getDecoder().decode(base64State);
        int updated = noteRepository.updateYjsState(noteId, state);
        if (updated == 0) {
            throw new EntityNotFoundException("Note not found: " + noteId);
        }
        log.debug("[collab] Saved Yjs state note={} size={}bytes", noteId, state.length);
    }
}
