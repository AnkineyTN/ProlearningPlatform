package com.cabybara.prolearningplatform.controller;

import com.cabybara.prolearningplatform.dto.request.note.RealTimeNoteUpdateRequestDTO;
import com.cabybara.prolearningplatform.dto.response.note.RealTimeNoteUpdateResponseDTO;
import com.cabybara.prolearningplatform.dto.response.note.ActiveUsersResponseDTO;
import com.cabybara.prolearningplatform.service.note.NoteService;
import com.cabybara.prolearningplatform.service.note.RealTimeNoteSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;

/**
 * WebSocket controller for real-time note collaboration
 * Handles STOMP messages for real-time editing
 */
@Controller
@Slf4j
@RequiredArgsConstructor
public class NoteWebSocketController {

    private final RealTimeNoteSessionService sessionService;
    private final SimpMessagingTemplate messagingTemplate;
    private final NoteService noteService;

    /**
     * Handle user joining a note editing session
     * Broadcast active users list to all users on that note
     */
    @MessageMapping("/notes/{setId}/{noteId}/join")
    public void userJoinNote(
            @DestinationVariable Long setId,
            @DestinationVariable Long noteId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        String sessionId = headerAccessor.getSessionId();
        // Extract user info from session attributes (set during subscription)
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");

        log.info("User {} ({}) joined note {} in set {}", userId, username, noteId, setId);
        sessionService.addUserSession(setId, noteId, sessionId, userId, username);

        // Broadcast updated active users list to all subscribers of this note
        ActiveUsersResponseDTO activeUsers = sessionService.getActiveUsers(noteId);
        messagingTemplate.convertAndSend("/topic/notes/" + setId + "/" + noteId + "/active-users", activeUsers);
    }

    /**
     * Handle real-time note content updates
     * Broadcast to all subscribed users on that note
     */
    @MessageMapping("/notes/{setId}/{noteId}/update")
    @SendTo("/topic/notes/{setId}/{noteId}")
    public RealTimeNoteUpdateResponseDTO updateNote(
            RealTimeNoteUpdateRequestDTO update,
            @DestinationVariable Long setId,
            @DestinationVariable Long noteId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String sessionId = headerAccessor.getSessionId();

        log.info("Note {} content updated by user {} ({})", noteId, userId, username);

        // Update cursor position
        Integer cursorPos = update.getContent() != null ? update.getContent().length() : 0;
        sessionService.updateUserCursor(noteId, sessionId, cursorPos);

        // Create response with user info
        RealTimeNoteUpdateResponseDTO response = new RealTimeNoteUpdateResponseDTO();
        response.setNoteId(noteId);
        response.setSetId(setId);
        response.setContent(update.getContent());
        response.setTitle(update.getTitle());
        response.setUpdatedByUserId(userId);
        response.setUpdatedByUsername(username);
        response.setTimestamp(System.currentTimeMillis());

        return response;
    }

    /**
     * Handle cursor position updates (for showing other users' cursor positions)
     */
    /**
     * Handle cursor position updates from frontend
     * Receives cursor position directly and broadcasts to all users
     */
    @MessageMapping("/notes/{setId}/{noteId}/cursor")
    @SendTo("/topic/notes/{setId}/{noteId}/cursors")
    public RealTimeNoteUpdateResponseDTO updateCursor(
            RealTimeNoteUpdateRequestDTO update,
            @DestinationVariable Long setId,
            @DestinationVariable Long noteId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        String sessionId = headerAccessor.getSessionId();

        // Use cursor position sent by frontend
        Integer cursorPosition = update.getCursorPosition() != null ? update.getCursorPosition() : 0;
        sessionService.updateUserCursor(noteId, sessionId, cursorPosition);

        RealTimeNoteUpdateResponseDTO response = new RealTimeNoteUpdateResponseDTO();
        response.setNoteId(noteId);
        response.setSetId(setId);
        response.setCursorPosition(cursorPosition);
        response.setUpdatedByUserId(userId);
        response.setUpdatedByUsername(username);
        response.setTimestamp(System.currentTimeMillis());

        return response;
    }

    /**
     * Get active users list (request/response pattern)
     * No payload required - just destination variables and headers
     */
    @MessageMapping("/notes/{setId}/{noteId}/active-users")
    public void getActiveUsers(
            @DestinationVariable Long setId,
            @DestinationVariable Long noteId,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        ActiveUsersResponseDTO activeUsers = sessionService.getActiveUsers(noteId);
        messagingTemplate.convertAndSend("/topic/notes/" + setId + "/" + noteId + "/active-users", activeUsers);
    }

    /**
     * Handle user disconnect (close tab, close connection, etc.)
     * Clean up user session from all notes and broadcast updated active users list
     */
    @EventListener
    public void handleDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        
        if (sessionId == null) {
            return;
        }
        
        try {
            // Remove this session from all user sessions and get affected notes (noteId -> setId map)
            Map<Long, Long> affectedNotes = sessionService.removeUserSession(sessionId);
            log.info("WebSocket user disconnected: sessionId={}, affectedNotes={}", sessionId, affectedNotes);
            
            // Broadcast updated active users list to each affected note
            affectedNotes.forEach((noteId, setId) -> {
                try {
                    ActiveUsersResponseDTO activeUsers = sessionService.getActiveUsers(noteId);
                    messagingTemplate.convertAndSend("/topic/notes/" + setId + "/" + noteId + "/active-users", activeUsers);
                    log.debug("Broadcasted updated active users for note {} in set {}", noteId, setId);
                } catch (Exception e) {
                    log.error("Error broadcasting active users for note {} in set {}", noteId, setId, e);
                }
            });
        } catch (Exception e) {
            log.error("Error handling WebSocket disconnect", e);
        }
    }
}
