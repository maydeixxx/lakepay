package com.LakePayProj.chatService.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.LakePayProj.chatService.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Получение пользователя по id
     *
     * @return пользователь
     */
    public User getById(Long id) {
        try {
            // TODO: update to use gateway api
            String url = "http://user-service:8081/user_id/";
            String userResponse = restTemplate.getForObject(url + id, String.class);
            if (userResponse == null) return null;
            return objectMapper.readValue(userResponse, User.class);
        } catch (JsonProcessingException e) {
            log.error("Error parsing user response: {}", e.getMessage());
        } catch (RestClientException e) {
            log.error("Error contacting userService: {}", e.getMessage());
        }
        return null;
    }

    /**
     * Получение текущего пользователя
     *
     * @return текущий пользователь
     */
    public User getCurrentUser() {
        User principal = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.info(String.valueOf(principal));
        return principal;
    }
}