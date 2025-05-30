package com.lakepayProj.chatService.services;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakepayProj.chatService.enums.UserRole;
import com.lakepayProj.chatService.models.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class UserService {
    private final String lakePayUrl = "https://lakepay.ru";
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Получение пользователя по id
     *
     * @return пользователь
     */
    public User getById(Long id) {
        try {
            String userResponse = restTemplate.getForObject(lakePayUrl+ "/user_id/" + id, String.class);
            return objectMapper.readValue(userResponse, User.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    // Mock user
    public User getById() {
        // JWT = eyJhbGciOiJIUzI1NiJ9.eyJ1cmxQaG90byI6Imh0dHBzOi8vdC5tZS9pL3VzZXJwaWMvMzIwLzZndjRKRFd2ZThqS3VRQWQ0T2hIZEdPTURaeDdYX0ExQXR4TVNxN2x6bGo3anc2Q1VVbUZQamhDQXpJLUh6OEMuanBnIiwicm9sZSI6IlVzZXIiLCJ0Z0lkIjo1NTExMzExNDE5LCJpZCI6MTIzNDU2Nzg5LCJ1c2VybmFtZSI6InRoZWl0c2FzdGVscyIsInN1YiI6InRoZWl0c2FzdGVscyIsImlhdCI6MTc0ODU5NzQ1NSwiZXhwIjoxNzQ4NzQxNDU1fQ.qQFSY_pnnAngAqsO5rZ7gDqAGV4egBsKkF5xfeZ7MjI
        return new User(
                123456789L,
                5511311419L,
                "theitsastels",
                "https://t.me/i/userpic/320/6gv4JDWve8jKuQAd4OhHdGOMDZx7X_A1AtxMSq7lzlj7jw6CUUmFPjhCAzI-Hz8C.jpg",
                UserRole.User
        );
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        // Получение пользователя из контекста Spring Security
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}