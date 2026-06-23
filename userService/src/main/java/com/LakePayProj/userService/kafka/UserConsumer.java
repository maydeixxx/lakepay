package com.LakePayProj.userService.kafka;

import com.LakePayProj.userService.dto.UserDto;
import com.LakePayProj.userService.entity.User;
import com.LakePayProj.userService.mapper.IUserMapper;
import com.LakePayProj.userService.repository.IUserRepository;
import com.LakePayProj.userService.service.UserService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.NullArgumentException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

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
        processRequest(record, Long::parseLong, id -> List.of(userToMap(userRepository.findById(id))), "get_user_data_by_id_response");
    }

    @KafkaListener(topics = "get_user_data_by_id_request", groupId = "userData")
    public void processUserTelegramIdRequest(ConsumerRecord<String, String> record) {
        processRequest(record, Long::parseLong, tgId -> List.of(userToMap(userRepository.findByTelegramId(tgId))), "get_user_data_by_id_response");
    }

    @KafkaListener(topics = "get_user_data_by_username_request", groupId = "userData")
    public void processUserUsernameRequest(ConsumerRecord<String, String> record) {
        processRequest(record, value -> value, username -> List.of(userToMap(userRepository.findByUsername(username))), "get_user_data_by_username_response");
    }

    @KafkaListener(topicPartitions = @TopicPartition(partitions = {"0"}, topic = "get_sub_users"), groupId = "userData")
    public void processUserCategoriesRequest(ConsumerRecord<String, String> record) {
        List<Map<String, Object>> users = userService.findUsersByCategory(record.value()).stream()
                .map(this::userToMap)
                .toList();
        processRequest(record, value -> value, category -> userService.findUsersByCategory(category).stream().map(this::userToMap).toList(), "response_sub_users");
    }


    private <T> void processRequest(ConsumerRecord<String, String> record, java.util.function.Function<String, T> parser,
                                    java.util.function.Function<T, List<Map<String, Object>>> finder, String responseTopic) {
        T identifier;
        try {
            identifier = parser.apply(record.value());
        } catch (NumberFormatException e) {
            log.error("Invalid format for identifier in record.value: {}", record.value(), e);
            return;
        }

        List<Map<String, Object>> users = finder.apply(identifier);
        if (users.isEmpty()) {
            kafkaTemplate.send(responseTopic, identifier.toString(), null);
            log.warn("User with identifier {} not found. Sent null response.", identifier);
            return;
        }

        publishUserResponse(users, responseTopic, record.key());
    }


    private void publishUserResponse(List<Map<String, Object>> users, String topic, String recordKey) {
        try {
            String responseJson = objectMapper.writeValueAsString(users);
            if (users.size() <= 1) {
                kafkaTemplate.send(topic, users.getFirst().get("id").toString(), responseJson);
                log.info("Published UserResponse to {} for userId={}: {}", topic, users.getFirst().get("id").toString(), responseJson);
            } else {
                kafkaTemplate.send(topic, recordKey, responseJson);
                log.info("Published response: {} | in topic {} ", responseJson, topic);
            }

        } catch (JsonProcessingException e) {
            log.error("Error serializing UserResponse for userId={}: {}", users.getFirst().get("id").toString(), e.getMessage());
        }
    }

    private Map<String, Object> userToMap(Optional<User> optionalUser) {
        User user = optionalUser.orElseThrow(() -> new NullArgumentException(""));
        return Map.of(
                "id", user.getId(),
                "tgId", user.getTelegramId(),
                "username", user.getUsername(),
                "categories", user.getUsername()
        );
    }
}