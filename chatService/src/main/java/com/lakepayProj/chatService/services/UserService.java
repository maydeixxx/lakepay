package com.lakepayProj.chatService.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public User getById(String id) throws JsonProcessingException {
        String userResponse = restTemplate.getForObject(lakePayUrl+ "/user_id/" + id, String.class);
        return objectMapper.readValue(userResponse, User.class);
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