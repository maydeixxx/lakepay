package com.LakePayProj.userService.kafka;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.LakePayProj.userService.repository.IUserRepository;
import com.LakePayProj.userService.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConsumer {
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    private final UserService userService;
    private final IUserMapper userMapper;
    private final IUserRepository userRepository;

    @KafkaListener(topics = "get_user_data_by_id_request", groupId = "userData")
    public void handleUserId(ConsumerRecord<String, String> record) {
        Long userId;

        try {
            userId = Long.parseLong(record.value());
        } catch (NumberFormatException e) {
            log.error("Некорректный формат userId в record.value: {}", record.value(), e);
            return;
        }

        Optional<User> optionalUser = userService.findById(userId);

        if (optionalUser.isEmpty()) {
            template.send("get_user_data_by_id_response", userId.toString(), null);
            log.warn("Пользователь с userId={} не найден. Отправлен null-ответ.", userId);
            return;
        }

        sendUserResponse(optionalUser.get(), "get_user_data_by_id_response");
    }

    @KafkaListener(topics = "get_user_data_by_telegramId_request", groupId = "userDataTelegramId")
    public void handleTelegramId(ConsumerRecord<String, String> record) {
        Long telegramId;

        try {
            telegramId = Long.parseLong(record.value());
        } catch (NumberFormatException e) {
            log.error("Некорректный формат telegramId в record.value: {}", record.value(), e);
            return;
        }

        Optional<User> optionalUser = userRepository.findByTelegramId(telegramId);

        if (optionalUser.isEmpty()) {
            template.send("get_user_data_by_telegramId_response", telegramId.toString(), null);
            log.warn("Пользователь с telegramId={} не найден. Отправлен null-ответ.", telegramId);
            return;
        }

        sendUserResponse(optionalUser.get(), "get_user_data_by_telegramId_response");
    }

    @KafkaListener(topics = "get_user_data_by_username_request", groupId = "userDataUsername")
    public void handleUsername(ConsumerRecord<String, String> record) {
        String username = record.value();

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            template.send("get_user_data_by_username_response", username, null);
            log.warn("Пользователь с username='{}' не найден. Отправлен null-ответ.", username);
            return;
        }

        sendUserResponse(optionalUser.get(), "get_user_data_by_username_response");
    }

    private void sendUserResponse(User user, String topic) {
        UserDto response = userMapper.toDto(user);
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            template.send(topic, user.getId().toString(), responseJson);
            log.info("Отправлен UserResponse в {} для userId={}: {}", topic, user.getId(), responseJson);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации UserResponse для userId={}: {}", user.getId(), e.getMessage());
        }
    }
}
