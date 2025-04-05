package com.lakepayProj.userService.application.kafka;

import com.lakepayProj.userService.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProducer {
    private final KafkaTemplate<String, String> template;

    public void sendUser(User user) {
        String message = user.getUserName() + ", вы успешно зарегистрировались!";
        template.send("usersLog", String.valueOf(user.getChatId()), message);
    }
}
