package com.LakePayProj.notificationService.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramProducer {
    private final KafkaTemplate<String, String> template;

    public void sendTgAndChatId(Long tgId, Long chatId) {
        template.send("userTgChatId", String.valueOf(tgId), String.valueOf(chatId));
    }
}
