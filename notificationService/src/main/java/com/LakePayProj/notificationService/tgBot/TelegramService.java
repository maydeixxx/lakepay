package com.LakePayProj.notificationService.tgBot;

import com.LakePayProj.notificationService.kafka.TelegramProducer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService extends TelegramLongPollingBot {
    private final TelegramProducer producer;
    private final List<String> categories = List.of("PUBG", "CS2", "FORTNITE");

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
                    new BotCommand("/start", "запуск бота"),
                    new BotCommand("/reg", "получить ссылку на регистрацию на сайте"),
                    new BotCommand("/help", "получить список команд"),
                    new BotCommand("/info", "получить информацию про LakePay"),
                    new BotCommand("/categories", "список доступных категорий"),
                    new BotCommand("/subscribe", "подписка на категорию (выбор по кнопке)"),
                    new BotCommand("/unsubscribe", "отписка от категории (выбор по кнопке)"),
                    new BotCommand("/availableAds", "доступные объявления")
            );
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Ошибка при установке команд: ", e);
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

    private void handleMessage(Update update) throws TelegramApiException {
        Long chatId = update.getMessage().getChatId();
        Long tgId = update.getMessage().getFrom().getId();
        String text = update.getMessage().getText().trim();

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        switch (text) {
            case "/start" -> sendMessage.setText("Привет! Я LakePayBot, выбери команду для взаимодействия.");
            case "/reg" -> sendMessage.setText("Вот твоя ссылка на регистрацию:\nhttps://quickly-resilient-planthopper.cloudpub.ru/userService/auth/telegram");
            case "/help" -> sendMessage.setText("Список команд:\n/start\n/reg\n/help\n/info\n/categories\n/subscribe\n/unsubscribe");
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
            case "/availableAds" -> {
                producer.availableAds(tgId);
            }
            default -> sendMessage.setText("Неизвестная команда. Напишите /help для списка доступных команд.");
        }

        producer.sendTgAndChatId(tgId, chatId);

        if (sendMessage.getText() != null) {
            execute(sendMessage);
        }
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