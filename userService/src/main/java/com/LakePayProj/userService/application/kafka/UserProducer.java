package com.LakePayProj.userService.application.kafka;

import com.LakePayProj.userService.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProducer {
    private final KafkaTemplate<String, String> template;

    public void sendUser(User user) {
        String message = user.getUsername() + ", вы успешно зарегистрировались!";
        template.send("usersLog", String.valueOf(user.getChatId()), message);
    }

    public void sendInfoAboutSub(Long chatId, String category) {
        String message = "🎮Вы успешно подписались на категорию: " + category + "🎮";
        template.send("ads-sub", 0, chatId.toString(),  message);
    }

    public void sendInfoAboutUnSub(Long chatId, String category) {
        String message = "🎮Вы успешно отписались от категории: " + category + "🎮";
        template.send("ads-sub", 1, chatId.toString(),  message);
    }
}
