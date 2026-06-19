package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.response.share.VerifyAccessResponse;
import com.cabybara.prolearningplatform.repository.NoteRepository;
import com.cabybara.prolearningplatform.service.note.impl.CollabServiceImpl;
import com.cabybara.prolearningplatform.service.permission.impl.NotePermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollabServiceImplTest {

    @Mock
    private NotePermissionService notePermissionService;

    @Mock
    private NoteRepository noteRepository;

    @Test
    void verifyAccessForOwnerReturnsGranted() {
        CollabServiceImpl service = new CollabServiceImpl(notePermissionService, noteRepository);

        when(notePermissionService.hasAccess(1L, 1L)).thenReturn(true);
        when(notePermissionService.getRole(1L, 1L)).thenReturn("OWNER");

        VerifyAccessResponse response = service.verifyAccess(1L, 1L);

        assertNotNull(response);
        assertEquals(1L, response.getUserId());
        assertEquals("OWNER", response.getRole());
    }

    @Test
    void verifyAccessForMemberReturnsGranted() {
        CollabServiceImpl service = new CollabServiceImpl(notePermissionService, noteRepository);

        when(notePermissionService.hasAccess(2L, 1L)).thenReturn(true);
        when(notePermissionService.getRole(2L, 1L)).thenReturn("EDITOR");

        VerifyAccessResponse response = service.verifyAccess(2L, 1L);

        assertNotNull(response);
        assertEquals(2L, response.getUserId());
        assertEquals("EDITOR", response.getRole());
    }

    @Test
    void verifyAccessForNonMemberReturnsDenied() {
        CollabServiceImpl service = new CollabServiceImpl(notePermissionService, noteRepository);

        when(notePermissionService.hasAccess(3L, 1L)).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> service.verifyAccess(3L, 1L));
    }

    @Test
    void saveYjsStatePersistsToDatabase() {
        CollabServiceImpl service = new CollabServiceImpl(notePermissionService, noteRepository);

        String base64State = Base64.getEncoder().encodeToString("yjs-binary-data".getBytes());
        byte[] expectedBytes = Base64.getDecoder().decode(base64State);

        when(noteRepository.updateYjsState(eq(1L), any(byte[].class))).thenReturn(1);

        service.saveYjsState(1L, base64State);

        verify(noteRepository).updateYjsState(1L, expectedBytes);
    }
}
