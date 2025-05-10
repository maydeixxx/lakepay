package com.lakepayProj.chatService.config;

import com.lakepayProj.chatService.services.WsAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class WsAuthHandshakeInterceptor implements HandshakeInterceptor {
    Logger logger = LoggerFactory.getLogger(WsAuthHandshakeInterceptor.class);
    private final WsAuthService authService = new WsAuthService();

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        try {
            if (request instanceof ServletServerHttpRequest servletRequest) {
                // Extract token from query parameters
                HttpServletRequest req = servletRequest.getServletRequest();
                String token = req.getParameter("token");

                if (token == null) {
                    return false;
                }
                logger.info("Received handshake with token: " + token);

                // Validate token and get username
                String username = authService.validateTokenAndGetUsername(token);
                // Store the Principal in the session attributes
                attributes.put("username", username);
                return true; // Allow handshake
            } else {
                return false;
            }
        } catch (IllegalArgumentException e) {
            return false; // Reject handshake
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}