package com.LakePayProj.notificationService.services;

import com.LakePayProj.notificationService.exceptions.SendMessageException;
import com.LakePayProj.notificationService.exceptions.UserExistsInHash;
import com.LakePayProj.notificationService.kafka.TelegramProducer;
import com.LakePayProj.notificationService.models.redis.UserRedis;
import com.LakePayProj.notificationService.repos.UserRedisRepo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
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
    private final UserRedisService userRedisService;
    private final UserRedisRepo userRedisRepo;
    private final List<String> categories = List.of("PUBG", "CS2", "FORTNITE", "DEADLOCK", "DOTA2");
    private final ObjectMapper objectMapper;

    @Override
    public String getBotUsername() {
        return "@lakePayBot";
    }

    @Value("${token.telegram.bot}")
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
        if (userRedisRepo.findByTgId(tgId.toString()).isEmpty()) {
            userRedisService.saveUser(
                    UserRedis.builder()
                            .tgId(tgId.toString())
                            .chatId(chatId.toString())
                            .build()
            );
        }

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
            if (userRedisRepo.findUserRedisById(userId.toString()).isEmpty()) {
                producer.getUserData(userId);
            }
            String chatId = userRedisService.findUserById(userId.toString()).getChatId();
            Thread.sleep(2000);
            sendMessage(chatId, "Ссылка на оплату: " + payUrl);
            log.info("Отправлена ссылка на оплату: userId={}, chatId={}, payUrl={}", userId, chatId, payUrl);
        } catch (Exception e) {
            log.error("Ошибка отправки ссылки на оплату: userId={}, error={}", userId, e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "get_user_data_by_id_response", groupId = "userData")
    public void getUserDataResponse(ConsumerRecord<String, String> record) {
        try {
            List<Map<String, Object>> listUsers = objectMapper.readValue(record.value(), new TypeReference<>() {});
            Map<String, Object> userData = listUsers.getFirst();
            userRedisService.saveUser(
                    UserRedis.builder()
                            .id(userData.get("id").toString())
                            .chatId(userData.get("tgId").toString())
                            .tgId(userData.get("tgId").toString())
                            .build()
            );
            log.info("Записан chatId = {}", record.value());
        } catch (Exception e) {
            log.error("Ошибка при получении сообщения в get_user_data_by_id_telegram_response. {}", e.getMessage());
        }
    }

    public void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setText(message);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            throw new SendMessageException(String.format("failed to send message to %s", chatId));
        }
    }
}