package com.LakePayProj.userService.kafka;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.LakePayProj.userService.repository.IUserRepository;
import com.LakePayProj.userService.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final UserService userService;
    private final IUserMapper userMapper;
    private final IUserRepository userRepository;

    @KafkaListener(topics = "get_user_data_by_id_request", groupId = "userData")
    public void processUserIdRequest(ConsumerRecord<String, String> record) {
        processRequest(record, Long::parseLong, userService::findById, "get_user_data_by_id_response");
    }

    @KafkaListener(topics = "get_user_data_by_telegramId_request", groupId = "userData")
    public void processUserTelegramIdRequest(ConsumerRecord<String, String> record) {
        processRequest(record, Long::parseLong, userRepository::findByTelegramId, "get_user_data_by_telegramId_response");
    }

    @KafkaListener(topics = "get_user_data_by_username_request", groupId = "userData")
    public void processUserUsernameRequest(ConsumerRecord<String, String> record) {
        processRequest(record, value -> value, userRepository::findByUsername, "get_user_data_by_username_response");
    }

    private <T> void processRequest(ConsumerRecord<String, String> record, java.util.function.Function<String, T> parser,
                                    java.util.function.Function<T, Optional<User>> finder, String responseTopic) {
        T identifier;
        try {
            identifier = parser.apply(record.value());
        } catch (NumberFormatException e) {
            log.error("Invalid format for identifier in record.value: {}", record.value(), e);
            return;
        }

        Optional<User> optionalUser = finder.apply(identifier);
        if (optionalUser.isEmpty()) {
            kafkaTemplate.send(responseTopic, identifier.toString(), null);
            log.warn("User with identifier {} not found. Sent null response.", identifier);
            return;
        }

        publishUserResponse(optionalUser.get(), responseTopic);
    }

    private void publishUserResponse(User user, String topic) {
        UserDto response = userMapper.toDto(user);
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(topic, user.getId().toString(), responseJson);
            log.info("Published UserResponse to {} for userId={}: {}", topic, user.getId(), responseJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing UserResponse for userId={}: {}", user.getId(), e.getMessage());
        }
    }
}