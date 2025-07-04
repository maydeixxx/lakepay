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
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
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
        processRequest(record, Long::parseLong, id -> List.of(userRepository.findById(id)), "get_user_data_by_id_response");
    }

    @KafkaListener(topics = "get_user_data_by_telegramId_request", groupId = "userData")
    public void processUserTelegramIdRequest(ConsumerRecord<String, String> record) {
        processRequest(record, Long::parseLong, tgId -> List.of(userRepository.findByTelegramId(tgId)), "get_user_data_by_telegramId_response");
    }

    @KafkaListener(topics = "get_user_data_by_username_request", groupId = "userData")
    public void processUserUsernameRequest(ConsumerRecord<String, String> record) {
        processRequest(record, value -> value, username -> List.of(userRepository.findByUsername(username)), "get_user_data_by_username_response");
    }

    @KafkaListener(topicPartitions = @TopicPartition(partitions = {"0"}, topic = "get_sub_users"), groupId = "userData")
    public void processUserCategoriesRequest(ConsumerRecord<String, String> record) {
        processRequest(record, value -> value, userService::findUsersByCategory, "response_sub_users");
    }


    private <T> void processRequest(ConsumerRecord<String, String> record, java.util.function.Function<String, T> parser,
                                    java.util.function.Function<T, List<Optional<User>>> finder, String responseTopic) {
        T identifier;
        try {
            identifier = parser.apply(record.value());
        } catch (NumberFormatException e) {
            log.error("Invalid format for identifier in record.value: {}", record.value(), e);
            return;
        }

        List<Optional<User>> optionalUsers = finder.apply(identifier);
        if (optionalUsers.isEmpty()) {
            kafkaTemplate.send(responseTopic, identifier.toString(), null);
            log.warn("User with identifier {} not found. Sent null response.", identifier);
            return;
        }
        List<User> users = optionalUsers.stream()
                .map(user -> user.orElseThrow(() -> new NotFoundException("User not found")))
                .toList();

        publishUserResponse(users, responseTopic, record.key());
    }


    private void publishUserResponse(List<User> users, String topic, String recordKey) {
        try {
            List<UserDto> userDTO = users.stream()
                    .map(userMapper::toDto)
                    .toList();
            String responseJson = objectMapper.writeValueAsString(userDTO);
            if (userDTO.size() <= 1) {
                kafkaTemplate.send(topic, users.getFirst().getId().toString(), responseJson);
                log.info("Published UserResponse to {} for userId={}: {}", topic, users.getFirst().getId().toString(), responseJson);
            } else {
                kafkaTemplate.send(topic, recordKey, responseJson);
                log.info("Published response: {} | in topic {} ", responseJson, topic);
            }

        } catch (JsonProcessingException e) {
            log.error("Error serializing UserResponse for userId={}: {}", users.getFirst().getId().toString(), e.getMessage());
        }
    }
}