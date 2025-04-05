package com.LakePayProj.notificationService.kafka;

import com.LakePayProj.notificationService.tgBot.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {
    private final TelegramService service;

    @KafkaListener(topics = "usersLog", groupId = "user-notifications")
    public void sendNewUser(ConsumerRecord<String, String> record) {
        String chatId = record.key();
        String message = record.value();

        service.sendMessage(chatId, message);
    }
}
