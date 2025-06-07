package com.LakePayProj.notificationService.tgBot;

import com.LakePayProj.notificationService.kafka.TelegramProducer;
import com.LakePayProj.notificationService.configs.WebClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService extends TelegramLongPollingBot {
    private final TelegramProducer producer;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final List<String> categories = List.of("PUBG", "CS2", "FORTNITE", "DEADLOCK", "DOTA2");
    private String chatIdHash;

    @Override
    public String getBotUsername() {
        return "@lakePayBot";
    }

    private final String lakePayUrl = "https://lakepay.ru";

    @Value("${tgBot.token}")
    private String token;

    @Override
    public String getBotToken() {
        return token;
    }

    @PostConstruct
    public void initCommands() {
        try {
            List<BotCommand> commandList = List.of(
                    new BotCommand("/start", "Запуск бота"),
                    new BotCommand("/reg", "Получить ссылку на регистрацию на сайте"),
                    new BotCommand("/help", "Получить список команд"),
                    new BotCommand("/info", "Получить информацию о LakePay"),
                    new BotCommand("/categories", "Список доступных категорий"),
                    new BotCommand("/available_ads", "доступные объявления")
            );
            SetMyCommands setMyCommands = new SetMyCommands();
            setMyCommands.setCommands(commandList);
            setMyCommands.setScope(new BotCommandScopeDefault());
            setMyCommands.setLanguageCode(null);
            boolean result = execute(setMyCommands);
            log.info("Команды бота успешно установлены: {}", result);
        } catch (TelegramApiException e) {
            log.error("Ошибка при установке команд бота: {}", e.getMessage(), e);
            throw new RuntimeException("Не удалось установить команды бота", e);
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleMessage(update);
            }
        } catch (Exception e) {
            log.error("Ошибка в onUpdateReceived: ", e);
        }
    }

    private void handleMessage(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long tgId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();
        chatIdHash = String.valueOf(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        switch (text) {
            case "/start" -> sendMessage.setText("Привет! Я LakePayBot, выбери команду для взаимодействия.");
            case "/reg" -> sendMessage.setText("Вот твоя ссылка на регистрацию:\nlakepay.ru/auth/telegram");
            case "/help" ->
                    sendMessage.setText("Список команд:\n/start\n/reg\n/help\n/info\n/categories\n/subscribe\n/unsubscribe\n/available_ads\n/pay");
            case "/info" ->
                    sendMessage.setText("Я бот биржи аккаунтов LakePay. Через меня можно зарегистрироваться и получать уведомления о новых объявлениях.");
            case "/categories" -> sendMessage.setText("Доступные категории:\n" + String.join("\n", categories));
            case "/available_ads" -> {
                producer.availableAds(tgId);
            }
            default -> sendMessage.setText("Неизвестная команда. Напишите /help для списка доступных команд.");
        }

        producer.sendTgAndChatId(tgId, chatId);

        if (sendMessage.getText() != null) {
            execute(sendMessage);
        }
    }

    public void sendPaymentLink(Long userId, String payUrl) {
        try {
            String response = restTemplate.getForObject(lakePayUrl + "/user_id/" + userId, String.class);
            Map<String, Object> userData = objectMapper.readValue(response, Map.class);
            Long chatId = Long.valueOf(userData.get("chatId").toString());

            sendMessage(chatIdHash, "Ссылка на оплату: " + payUrl);
            log.info("Отправлена ссылка на оплату: userId={}, chatId={}, payUrl={}", userId, chatId, payUrl);
        } catch (Exception e) {
            log.error("Ошибка отправки ссылки на оплату: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    public void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения вручную: ", e);
        }
    }
}