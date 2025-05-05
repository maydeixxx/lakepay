package com.LakePayProj.notificationService.tgBot;

import com.LakePayProj.notificationService.kafka.TelegramProducer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService extends TelegramLongPollingBot {
    private final TelegramProducer producer;
    private final List<String> categories = List.of("PUBG", "CS2", "FORTNITE", "DEADLOCK", "DOTA2");
    private static final List<String> SUPPORTED_CURRENCIES = List.of("BTC", "USDT", "ETH", "TON");

    @Override
    public String getBotUsername() {
        return "@lakePayBot";
    }

    @Override
    public String getBotToken() {
        return "7906616449:AAGLMQphhjOTHCgyAW9d9xlV94vN-Deai54";
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
                    new BotCommand("/subscribe", "Подписка на категорию (выбор по кнопке)"),
                    new BotCommand("/unsubscribe", "Отписка от категории (выбор по кнопке)"),
                    new BotCommand("/available_ads", "Доступные объявления"),
                    new BotCommand("/pay", "Оплатить объявление (формат: /pay <adId> <currency>)")
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
            if (update.hasCallbackQuery()) {
                handleCallback(update);
            } else if (update.hasMessage() && update.getMessage().hasText()) {
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

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (text.startsWith("/pay")) {
            handlePayCommand(text, tgId, chatId, sendMessage);
        } else {
            // Остальные команды
            switch (text) {
                case "/start" -> sendMessage.setText("Привет! Я LakePayBot, выбери команду для взаимодействия.");
                case "/reg" -> sendMessage.setText("Вот твоя ссылка на регистрацию:\nlakepay.ru/auth/telegram");
                case "/help" -> sendMessage.setText("Список команд:\n/start\n/reg\n/help\n/info\n/categories\n/subscribe\n/unsubscribe\n/available_ads\n/pay");
                case "/info" -> sendMessage.setText("Я бот биржи аккаунтов LakePay. Через меня можно зарегистрироваться и получать уведомления о новых объявлениях.");
                case "/categories" -> sendMessage.setText("Доступные категории:\n" + String.join("\n", categories));
                case "/subscribe" -> {
                    sendMessage.setText("Выберите категорию для подписки:");
                    sendMessage.setReplyMarkup(categoryButtons("/subscribe"));
                }
                case "/unsubscribe" -> {
                    sendMessage.setText("Выберите категорию для отписки:");
                    sendMessage.setReplyMarkup(categoryButtons("/unsubscribe"));
                }
                case "/available_ads" -> producer.availableAds(tgId);
                default -> sendMessage.setText("Неизвестная команда. Напишите /help для списка доступных команд.");
            }
        }

        producer.sendTgAndChatId(tgId, chatId);

        if (sendMessage.getText() != null) {
            execute(sendMessage);
        }
    }

    private void handlePayCommand(String text, Long tgId, Long chatId, SendMessage sendMessage) {
        String[] parts = text.split("\\s+");
        if (parts.length != 3) {
            sendMessage.setText("Неверный формат. Используйте: /pay <adId> <currency>\nПример: /pay 123 BTC");
            return;
        }

        try {
            Long adId = Long.parseLong(parts[1]);
            String currency = parts[2].toUpperCase();

            if (!SUPPORTED_CURRENCIES.contains(currency)) {
                sendMessage.setText("Неподдерживаемая валюта. Доступные валюты: " + String.join(", ", SUPPORTED_CURRENCIES));
                return;
            }

            // Отправляем запрос на создание платежа в paymentService через Kafka
            producer.sendPaymentRequest(tgId, adId, currency, 0.0001); // Фиксированная сумма для примера
            sendMessage.setText("Запрос на оплату отправлен. Ожидайте ссылку для оплаты.");
        } catch (NumberFormatException e) {
            sendMessage.setText("Ошибка: adId должен быть числом. Пример: /pay 123 BTC");
        }
    }

    private void handleCallback(Update update) throws TelegramApiException {
        Long chatId = update.getCallbackQuery().getMessage().getChatId();
        Long tgId = update.getCallbackQuery().getFrom().getId();
        String callbackData = update.getCallbackQuery().getData();

        String[] parts = callbackData.split(":");
        String action = parts[0];
        String category = parts[1];

        if (action.equals("/subscribe")) {
            producer.sendCategoryToSubscribe(tgId, category);
        } else if (action.equals("/unsubscribe")) {
            producer.sendToUNSUB(tgId, category);
        }

        EditMessageText message = new EditMessageText();
        message.setChatId(chatId);
        message.setMessageId(update.getCallbackQuery().getMessage().getMessageId());
        message.setText("Вы " + (action.equals("/subscribe") ? "подписались на " : "отписались от ") + category);
        execute(message);
    }

    private InlineKeyboardMarkup categoryButtons(String action) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String category : categories) {
            InlineKeyboardButton button = new InlineKeyboardButton();
            button.setText(category);
            button.setCallbackData(action + ":" + category);
            rows.add(List.of(button));
        }
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);
        return markup;
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