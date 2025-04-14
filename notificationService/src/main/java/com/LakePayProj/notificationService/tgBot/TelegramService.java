package com.LakePayProj.notificationService.tgBot;

import com.LakePayProj.notificationService.kafka.TelegramProducer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramService extends TelegramLongPollingBot {
    private final TelegramProducer producer;

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
                    new BotCommand("/info", "получить информацию про LakePay")
            );
            this.execute(new SetMyCommands(commandList, new BotCommandScopeDefault(), null));

        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            Long tgId = update.getMessage().getFrom().getId();
            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(chatId);


            switch (update.getMessage().getText()) {
                case "/start" ->
                        sendMessage.setText("Привет! Я LakePayBot, выбери команду чтобы дальше взаимодействовать со мной\n");
                case "/reg" ->
                        sendMessage.setText("Вот твоя ссылка на регистрацию! -> https://right-terminally-humpback.ngrok-free.app/userService/auth/telegram");
                case "/help" -> sendMessage.setText("Вот список команд:\n/start\n/reg\n/help\n/info");
                case "/info" ->
                        sendMessage.setText("Я бот биржи аккаунтов LakePay!\nЧерез меня можно зарегистрироваться на сайте и получать информацию на новых объявлениях по аккаунтам!");
            }

            try {
                producer.sendTgAndChatId(tgId, chatId);
                execute(sendMessage);
            } catch (TelegramApiException e) {
                log.error("Ошибка при отправке сообщения: ", e);
            }
        }
    }

    public void sendMessage(String chatId, String message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText(message);
        sendMessage.setChatId(chatId);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            log.error("Ошибка при отправке сообщения: ", e);
        }
    }
}
