package com.LakePayProj.notificationService.kafka;

import com.LakePayProj.notificationService.tgBot.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {
    private final TelegramService service;
    private final WebClient webClient;

    @KafkaListener(topics = "usersLog", groupId = "user-notifications")
    public void sendNewUser(ConsumerRecord<String, String> record) {
        String chatId = record.key();
        String message = record.value();

        service.sendMessage(chatId, message);
    }

    public List<String> getSubScribedUsers(String category) {
        return webClient.get()
                .uri("https://right-terminally-humpback.ngrok-free.app/userService/category/{category}", category)
                .retrieve()
                .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(userMap -> String.valueOf(userMap.get("chatId")))
                .collectList()
                .block();
    }

    @KafkaListener(topics = "ads", groupId = "ads-sub")
    public void sendNewAds(ConsumerRecord<String, String> record) {
        List<String> subScribedUsers = getSubScribedUsers(record.key());
        subScribedUsers.forEach(user -> service.sendMessage(user, record.value()));
    }
}
