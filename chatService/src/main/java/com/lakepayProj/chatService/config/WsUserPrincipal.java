package com.lakepayProj.chatService.config;

import java.security.Principal;

// This class holds context of the currently logged-in user
public class WsUserPrincipal implements Principal {
    private final String username;

    public WsUserPrincipal(String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }
}