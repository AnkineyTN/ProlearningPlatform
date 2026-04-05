package com.cabybara.prolearningplatform.service.note;

import com.cabybara.prolearningplatform.dto.response.note.ActiveUserDTO;
import com.cabybara.prolearningplatform.dto.response.note.ActiveUsersResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing real-time note editing sessions
 * Tracks active users and broadcasts updates
 */
@Service
@Slf4j
public class RealTimeNoteSessionService {

    /**
     * Structure: noteId -> Map<sessionId, ActiveUserDTO>
     */
    private final ConcurrentHashMap<Long, ConcurrentHashMap<String, ActiveUserDTO>> activeSessions = new ConcurrentHashMap<>();

    /**
     * Structure: sessionId -> Set of "setId:noteId" pairs
     * Used to track which notes a session is editing and their setIds
     */
    private final ConcurrentHashMap<String, Set<String>> sessionNoteMapping = new ConcurrentHashMap<>();

    /**
     * Add a user session to a note
     */
    public void addUserSession(Long setId, Long noteId, String sessionId, Long userId, String username) {
        activeSessions
                .computeIfAbsent(noteId, k -> new ConcurrentHashMap<>())
                .putIfAbsent(sessionId, new ActiveUserDTO(userId, username, sessionId, 0, System.currentTimeMillis()));

        // Track which notes this session is editing
        String key = setId + ":" + noteId;
        sessionNoteMapping.computeIfAbsent(sessionId, k -> ConcurrentHashMap.newKeySet()).add(key);

        log.info("User {} joined note {} (set {}) with session {}", userId, noteId, setId, sessionId);
    }

    /**
     * Remove a user session from all notes (called on disconnect)
     * Returns a map of noteId -> setId for all affected notes
     */
    public Map<Long, Long> removeUserSession(String sessionId) {
        Map<Long, Long> affectedNotes = new HashMap<>();
        Set<String> notePairs = sessionNoteMapping.remove(sessionId);
        
        if (notePairs != null) {
            for (String pair : notePairs) {
                String[] parts = pair.split(":");
                if (parts.length == 2) {
                    Long setId = Long.parseLong(parts[0]);
                    Long noteId = Long.parseLong(parts[1]);
                    affectedNotes.put(noteId, setId);
                    
                    ConcurrentHashMap<String, ActiveUserDTO> sessions = activeSessions.get(noteId);
                    if (sessions != null) {
                        ActiveUserDTO removed = sessions.remove(sessionId);
                        if (removed != null) {
                            log.info("User {} disconnected from note {} (set: {}, session: {})", 
                                    removed.getUserId(), noteId, setId, sessionId);
                        }
                        // Clean up empty note sessions
                        if (sessions.isEmpty()) {
                            activeSessions.remove(noteId);
                        }
                    }
                }
            }
        }
        
        return affectedNotes;
    }

    /**
     * Remove a user session from a specific note
     */
    public void removeUserSession(Long setId, Long noteId, String sessionId) {
        ConcurrentHashMap<String, ActiveUserDTO> sessions = activeSessions.get(noteId);
        if (sessions != null) {
            ActiveUserDTO removed = sessions.remove(sessionId);
            if (removed != null) {
                log.info("User {} left note {} (set: {}) with session {}", removed.getUserId(), noteId, setId, sessionId);
            }
            // Clean up empty note sessions
            if (sessions.isEmpty()) {
                activeSessions.remove(noteId);
            }
        }
        
        // Clean up session note mapping
        String key = setId + ":" + noteId;
        Set<String> notePairs = sessionNoteMapping.get(sessionId);
        if (notePairs != null) {
            notePairs.remove(key);
            if (notePairs.isEmpty()) {
                sessionNoteMapping.remove(sessionId);
            }
        }
    }

    /**
     * Update cursor position for a user
     */
    public void updateUserCursor(Long noteId, String sessionId, Integer cursorPosition) {
        ConcurrentHashMap<String, ActiveUserDTO> sessions = activeSessions.get(noteId);
        if (sessions != null && sessions.containsKey(sessionId)) {
            ActiveUserDTO user = sessions.get(sessionId);
            user.setCursorPosition(cursorPosition);
            user.setLastActive(System.currentTimeMillis());
        }
    }

    /**
     * Get all active users for a note
     */
    public ActiveUsersResponseDTO getActiveUsers(Long noteId) {
        ConcurrentHashMap<String, ActiveUserDTO> sessions = activeSessions.get(noteId);
        if (sessions == null || sessions.isEmpty()) {
            return new ActiveUsersResponseDTO(noteId, Collections.emptyList(), 0);
        }

        List<ActiveUserDTO> activeUsers = new ArrayList<>(sessions.values());
        return new ActiveUsersResponseDTO(noteId, activeUsers, activeUsers.size());
    }

    /**
     * Get count of active users for a note
     */
    public int getActiveUserCount(Long noteId) {
        ConcurrentHashMap<String, ActiveUserDTO> sessions = activeSessions.get(noteId);
        return sessions != null ? sessions.size() : 0;
    }

    /**
     * Check if a note has active sessions
     */
    public boolean hasActiveSessions(Long noteId) {
        return activeSessions.containsKey(noteId) && !activeSessions.get(noteId).isEmpty();
    }

    /**
     * Remove stale sessions (inactive for more than specified milliseconds)
     */
    public void removeStaleSession(Long noteId, String sessionId, long maxInactiveMs) {
        ConcurrentHashMap<String, ActiveUserDTO> sessions = activeSessions.get(noteId);
        if (sessions != null) {
            ActiveUserDTO user = sessions.get(sessionId);
            if (user != null) {
                long inactiveTime = System.currentTimeMillis() - user.getLastActive();
                if (inactiveTime > maxInactiveMs) {
                    sessions.remove(sessionId);
                    log.info("Removed stale session {} from note {} (inactive for {} ms)", sessionId, noteId, inactiveTime);
                    // Clean up empty note sessions
                    if (sessions.isEmpty()) {
                        activeSessions.remove(noteId);
                    }
                    // Clean up from mapping - remove all entries for this session with this noteId
                    Set<String> notePairs = sessionNoteMapping.get(sessionId);
                    if (notePairs != null) {
                        notePairs.removeIf(pair -> pair.endsWith(":" + noteId));
                        if (notePairs.isEmpty()) {
                            sessionNoteMapping.remove(sessionId);
                        }
                    }
                }
            }
        }
    }
}
