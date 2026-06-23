package com.LakePayProj.userService.kafka;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final IUserMapper userMapper;

    public void publishCreateUser(User user) {
        publishUserEvent(user, "create_user");
    }

    public void publishUpdateUser(User user) {
        publishUserEvent(user, "update_user");
    }

    public void publishDeleteUser(Long userId) {
        try {
            String responseJson = objectMapper.writeValueAsString(userId);
            kafkaTemplate.send("delete_user", userId.toString(), responseJson);
            log.info("Published delete event for userId={} to 'delete_user': {}", userId, responseJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing delete event for userId={}: {}", userId, e.getMessage());
        }
    }

    private void publishUserEvent(User user, String topic) {
        UserDto response = userMapper.toDto(user);
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            kafkaTemplate.send(topic, user.getId().toString(), responseJson);
            log.info("Published {} event for userId={} to '{}': {}", topic, user.getId(), topic, responseJson);
        } catch (JsonProcessingException e) {
            log.error("Error serializing {} event for userId={}: {}", topic, user.getId(), e.getMessage());
        }
    }
}
