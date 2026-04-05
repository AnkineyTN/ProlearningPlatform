package com.cabybara.prolearningplatform.configuration;

import com.cabybara.prolearningplatform.service.auth.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

/**
 * Interceptor for extracting JWT token from WebSocket connection
 * Sets userId and username in session attributes
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtService jwtService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // Only process CONNECT messages
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            try {
                // Get JWT token from authorization header
                String authzHeader = accessor.getFirstNativeHeader("Authorization");
                
                if (authzHeader != null && authzHeader.startsWith("Bearer ")) {
                    String token = authzHeader.substring(7); // Remove "Bearer " prefix

                    // Validate token
                    if (jwtService.validateToken(token)) {
                        // Extract userId and email from token
                        Long userId = jwtService.extractUserId(token);
                        String email = jwtService.extractEmail(token);

                        // Set in session attributes
                        accessor.getSessionAttributes().put("userId", userId);
                        accessor.getSessionAttributes().put("username", email);

                        log.info("WebSocket user connected: userId={}, username={}", userId, email);
                    } else {
                        log.warn("Invalid JWT token provided for WebSocket connection");
                        throw new IllegalArgumentException("Invalid token");
                    }
                } else {
                    log.warn("No Authorization header provided for WebSocket connection");
                    throw new IllegalArgumentException("Missing authorization token");
                }
            } catch (Exception e) {
                log.error("Error processing WebSocket authentication", e);
                throw new IllegalArgumentException("Authentication failed: " + e.getMessage(), e);
            }
        }

        return message;
    }
}
