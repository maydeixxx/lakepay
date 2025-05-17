package com.lakepayProj.chatService.models;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class User {
    private long id;
    private String urlPhoto;
    private String role;
    private long tgId;
    private String username;
}
