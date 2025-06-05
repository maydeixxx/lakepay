package com.lakepayProj.userService.application.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lakepayProj.userService.application.services.UserService;
import com.lakepayProj.userService.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserConsumer {
    private final UserService userService;
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "get_user_data_by_id", groupId = "userData")
    public void handleUserId(ConsumerRecord<String, String> record) {
        User user = null;
        Long userId = Long.parseLong(record.value());
        if (record.partition() == 0) {
            user = userService.findUserById(userId);
        }
        try {
            if (user != null) {
                String response = objectMapper.writeValueAsString(Map.of(
                        "tgId", user.getTgId(),
                        "balance", user.getBalance()
                ));
                template.send("get_user_data_by_id", 1, "userIdResponse", response);
            }
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
    }
}
