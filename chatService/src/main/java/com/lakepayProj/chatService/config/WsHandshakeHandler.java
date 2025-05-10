package com.lakepayProj.chatService.config;

import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

public class WsHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        // Get username from attributes, previously set by your AuthHandshakeInterceptor
        String username = (String) attributes.get("username");

        // Create and return a custom Principal
        return new WsUserPrincipal(username);
    }
}
