package com.LakePayProj.notificationService.tgBot.config;

import com.LakePayProj.notificationService.tgBot.TelegramService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class TelegramBotConfig {
    private final TelegramService service;

    @PostConstruct
    public void regBot() {
        try {
            TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(service);
        } catch (TelegramApiException e) {
            log.error("чота не так{}", e.getMessage());
        }
    }
}
