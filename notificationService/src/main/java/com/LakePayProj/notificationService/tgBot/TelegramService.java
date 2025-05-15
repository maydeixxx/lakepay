package com.LakePayProj.notificationService.tgBot;

import com.LakePayProj.notificationService.kafka.TelegramProducer;
import com.LakePayProj.notificationService.configs.WebClientConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
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

import java.math.BigDecimal;
import java.util.ArrayList;
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
    private static final List<String> SUPPORTED_CURRENCIES = List.of("USDT");
    private final WebClientConfig webClientConfig;
    private String chatIdHash;

    @Override
    public String getBotUsername() {
        return "@lakePayBot";
    }

    private final String lakePayUrl = "https://lakepay.ru";

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
                    new BotCommand("/pay", "Оплатить объявление (формат: /pay <adId> <currency>)"),
                    new BotCommand("/deposit", "пополнить баланс (формат: /deposit <amount> <currency>)"),
                    new BotCommand("/withdraw", "вывод средств (формат: /withdraw <amount> <currency>)")
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
        chatIdHash = String.valueOf(chatId);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        if (text.startsWith("/pay")) {
            handlePayCommand(text, tgId, chatId);

        } else if (text.startsWith("/deposit")) {
            handleDepositRequest(text, tgId, chatId);
        } else {
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

    private void handlePayCommand(String messageText, Long tgId, Long chatId) {
        try {
            String[] parts = messageText.split(" ");
            if (parts.length != 3) {
                sendMessage(chatIdHash, "Формат: /pay <adId> <asset>");
            }

            Long adId = Long.parseLong(parts[1]);
            String asset = parts[2].toUpperCase();
            String adUrl = lakePayUrl + "/ad_id/" + adId;
            String adResponse = restTemplate.getForObject(adUrl, String.class);
            Map<String, Object> adData = objectMapper.readValue(adResponse, Map.class);
            BigDecimal price = new BigDecimal(adData.get("price").toString());

            producer.sendTgAndChatId(tgId, chatId);

            String response = restTemplate.getForObject(lakePayUrl + "/user_tg/" + tgId, String.class);
            Map<String, Object> userData = objectMapper.readValue(response, Map.class);
            Long userId = Long.valueOf(userData.get("id").toString());

            producer.sendPaymentRequest(userId, adId, asset, price.doubleValue());
            sendMessage(chatIdHash, "Запрос на оплату отправлен. Ожидайте ссылку.");
        } catch (Exception e) {
            log.error("Ошибка обработки /pay: {}", e.getMessage(), e);
            sendMessage(chatIdHash, "Ошибка при обработке оплаты. Проверьте параметры.");
        }
    }

    public void handleDepositRequest(String message, Long tgId, Long chatId) {
        try {
            String[] parts = message.split(" ");
            if (parts.length != 3) {
                sendMessage(String.valueOf(chatId), "формат: /deposit <amount> <currency>)");
            }

            Double amount = Double.parseDouble(parts[1]);
            String currency = parts[2].toUpperCase();

            producer.sendTgAndChatId(tgId, chatId);

            String response = restTemplate.getForObject(lakePayUrl + "/user_tg/" + tgId, String.class);
            Map<String, Object> userData = objectMapper.readValue(response, Map.class);
            Long userId = Long.valueOf(userData.get("id").toString());

            producer.sendDepositRequest(userId, amount, currency);
            sendMessage(String.valueOf(chatId), "запрос на пополнение отправлен, ожидайте ссылку");
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    public void handleWithdrawRequest(String message, Long tgId, Long chatId) {
        try {
            String[] parts = message.split(" ");

            if (parts.length != 3) {
                sendMessage(String.valueOf(chatId), "Формат: /withdraw <amount> <currency>");
            }

            Double amount = Double.parseDouble(parts[1]);
            String currency = parts[2];

            String response = restTemplate.getForObject(lakePayUrl + "/user_tg/" + tgId, String.class);
            Map<String, Object> userData = objectMapper.readValue(response, Map.class);
            Long userId = Long.valueOf(userData.get("id").toString());

            producer.sendWithdrawRequest(userId, amount, currency);
        } catch (Exception e) {
            log.error(e.getMessage());
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

    private Map<String, Object> getAdById(Long adId) {
        WebClient webClient = webClientConfig.webClient();
        try {
            return webClient.get()
                    .uri("https://lakepay.ru/ad_id/{adId}", adId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Ошибка при получении объявления adId={}: {}", adId, e.getMessage());
            return null;
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