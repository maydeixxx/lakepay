package com.lakepayProj.chatService.config;

import java.security.Principal;

// This class holds context of the currently logged-in user
public class UserPrincipal implements Principal {
    private final String username;

    public UserPrincipal(String username) {
        this.username = username;
    }

    @Override
    public String getName() {
        return username;
    }
}