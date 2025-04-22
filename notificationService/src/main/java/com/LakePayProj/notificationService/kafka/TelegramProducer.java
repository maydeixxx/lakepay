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

    public void sendCategoryToSubscribe(Long tgId, String category) {
        template.send("ads-sub", 0, String.valueOf(tgId), category);
    }

    public void sendToUNSUB(Long tgId, String category) {
        template.send("ads-sub", 1, String.valueOf(tgId), category);
    }

    public void availableAds(Long tgId) {
        template.send("availableAds", String.valueOf(tgId));
    }
}