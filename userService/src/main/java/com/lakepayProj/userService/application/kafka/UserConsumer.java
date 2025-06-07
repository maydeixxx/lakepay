package com.lakepayProj.userService.application.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakepayProj.userService.application.interfaces.repos.IUserRepository;
import com.lakepayProj.userService.application.services.UserService;
import com.lakepayProj.userService.domain.model.User;
import com.lakepayProj.userService.infrastructure.UserEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConsumer {
    private final UserService userService;
    private final IUserRepository userRepository;
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "get_user_data_by_id", groupId = "userData")
    public void handleUserId(ConsumerRecord<String, String> record) {
        try {
            Long userId = Long.parseLong(record.value());
            User user = userService.findUserById(userId);
            log.info("USER = {}", user);
            if (user != null) {
                String response = objectMapper.writeValueAsString(Map.of(
                        "tgId", user.getTgId(),
                        "balance", user.getBalance()
                ));
                template.send("responseToUserData", user.getId().toString(), response);
                log.info("Отправлен ответ в responseToUserData для userId={}: {}", userId, response);
            } else {
                log.warn("Пользователь с userId={} не найден", userId);
            }
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации данных пользователя: {}", e.getMessage());
        } catch (NumberFormatException e) {
            log.error("Некорректный формат userId в record.value: {}", record.value(), e);
        }
    }

    @KafkaListener(topics = "get_sub_users", groupId = "subscribed_users")
    public void handleSubscribedUsersRequest(ConsumerRecord<String, String> record) {
        String category = record.value();
        List<User> users = userService.findUserBySubs(category);
        List<Long> chatIds = users.stream().map(User::getChatId).toList();
        try {
            String response = objectMapper.writeValueAsString(chatIds);
            template.send("response_sub_users", "users", response);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "get_categories_byTg_request", groupId = "categories_tg")
    public void responseCategoriesByTg(ConsumerRecord<String, String> record) {
        Long tgId = Long.parseLong(record.value());
        try {
            UserEntity userByTgId = userRepository.findUserByTgId(tgId);
            List<String> subscriptions = userByTgId.getSubscriptions();
            String response = objectMapper.writeValueAsString(subscriptions);
            template.send("get_categories_byTg_response", tgId.toString(), response);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
