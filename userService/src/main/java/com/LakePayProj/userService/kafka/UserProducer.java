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

@Service
@Slf4j
@RequiredArgsConstructor
public class UserProducer {
    private final KafkaTemplate<String, String> template;
    private final ObjectMapper objectMapper;

    private final IUserMapper userMapper;

    public void sendUser(User user) {
        UserDto response = userMapper.toDto(user);
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            template.send("usersLog", user.getId().toString(), responseJson);
            log.info("Отправлен UserResponse в 'usersLog' для userId={}: {}", user.getId(), responseJson);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации UserResponse для userId={}: {}", user.getId(), e.getMessage());
        }
    }
}
