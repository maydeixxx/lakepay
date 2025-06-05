package com.LakePayProj.notificationService.kafka;

import com.LakePayProj.notificationService.tgBot.TelegramService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {
    private final TelegramService service;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;
    private Long hashTgId;
    private final TelegramProducer producer;
    private final List<String> cacheSubscribedUsers = new ArrayList<>();

    @KafkaListener(topics = "usersLog", groupId = "user-notifications")
    public void sendNewUser(ConsumerRecord<String, String> record) {
        String chatId = record.key();
        String message = record.value();
        service.sendMessage(chatId, message);
        log.debug("Отправлено сообщение для chatId={}: {}", chatId, message);
    }

    @KafkaListener(topics = "ad_data", groupId = "MONEY")
    public void sendAdDataToUser(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            String tgId = data.get("tgId").toString();
            String adId = data.get("adId").toString();
            String login = data.get("login").toString();
            String password = data.get("password").toString();
            StringBuilder message = new StringBuilder();
            message.append("Успешная покупка объявления 🆔 " + adId + "\n" + "*Данные от аккаунта*\n").append("login: ").append(login).append("\n")
                    .append("password: ").append(password);
            service.sendMessage(tgId, message.toString().trim());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "payment_created", groupId = "notification-group")
    public void handlePaymentCreated(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            Long userId = Long.valueOf(data.get("userId").toString());
            String payUrl = (String) data.get("payUrl");
            service.sendPaymentLink(userId, payUrl);
            log.info("Отправлена ссылка на оплату: userId={}, payUrl={}", userId, payUrl);
        } catch (Exception e) {
            log.error("Ошибка обработки payment_created: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "deposit_confirmed", groupId = "MONEY")
    public void handleSuccessfulDeposit(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            data.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
            StringBuilder message = new StringBuilder();
            String chatId = data.get("chatId").toString();
            log.info("chatId = {}", chatId);
            String amount = data.get("amount").toString();
            String asset = data.get("currency").toString();
            String balance = data.get("balance").toString();
            message.append("🤑Successful deposit🤑\n");
            message.append("Amount: ").append(amount).append("\n");
            message.append("Asset: ").append(asset).append("\n");
            message.append("Your current balance: ").append(balance).append("$ 💵");
            service.sendMessage(chatId, message.toString().trim());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "ads-sub", groupId = "subs", properties = {"partition.assignment.strategy=org.apache.kafka.clients.consumer.RangeAssignor"})
    public void consumeSub(ConsumerRecord<String, String> record) {
        int partition = record.partition();
        String chatId = record.key();
        String message = record.value();
        switch (partition) {
            case 0, 1 -> service.sendMessage(chatId, message);
            default -> log.error("Неверный выбор partition");
        }
    }

    @KafkaListener(topics = "availableAds", groupId = "user-notifications")
    public void saveTgId(ConsumerRecord<String, String> record) {
        hashTgId = Long.valueOf(record.value());
        getAdsByCategories();
        log.debug("Получен tgId для availableAds: {}", hashTgId);
    }

    @KafkaListener(topics = "ads", groupId = "ads-sub")
    public void sendNewAds(ConsumerRecord<String, String> record) {
        producer.getUsers(record.key());
        try {
            Thread.sleep(2000);
            log.info(cacheSubscribedUsers.toString());
            cacheSubscribedUsers.forEach(user -> {
                service.sendMessage(String.valueOf(user), record.value());
            });
            log.info("Отправлены уведомления о новых объявлениях для категории {}: {} пользователей", record.key(), cacheSubscribedUsers);
        } catch (Exception e) {
            log.error("Error = {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "withdraw_confirmed", groupId = "MONEY")
    public void handleSuccessfulWithdraw(ConsumerRecord<String, String> record){
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            String userId = data.get("userId").toString();
            String chatId = data.get("chatId").toString();
            String amount = data.get("amount").toString();
            String currency = data.get("currency").toString();
            String balance = data.get("balance").toString();
            StringBuilder message = new StringBuilder();
            message.append("💸 Средства успешно выведены 💸\n");
            message.append("Сумма: ").append(amount).append("\n");
            message.append("Валюта: ").append(currency).append("\n");
            message.append("Ваш текущий баланс: ").append(balance).append("$ 💵");
            service.sendMessage(chatId, message.toString().trim());
            log.info("Уведомление о выводе отправлено: userId={}, chatId={}, amount={}, currency={}",userId, chatId, amount, currency);
        }
        catch (Exception e){
            log.error("Ошибка обработки withdraw_confirmed");
        }
    }

    @KafkaListener(topics = "response_sub_users", groupId = "subscribed_users")
    public void getSubScribedUsers(ConsumerRecord<String, String> record) {
        try {
            List<Integer> chatIds = objectMapper.readValue(record.value(), List.class);
            cacheSubscribedUsers.addAll(chatIds.stream().map(String::valueOf).toList());

            log.info(cacheSubscribedUsers.toString());
        } catch (Exception e) {
            log.error("Не удалось получить подписчиков категории {}: {}", record.value(), e.getMessage());
        }
    }

    public List<String> getCategoriesByTgId(Long tgId) {
        try {
            return webClient.get()
                    .uri("https://lakepay.ru/categoriesById/{tgId}", tgId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<String>>() {})
                    .block();
        } catch (Exception e) {
            log.error("Не удалось получить категории для tgId {}: {}", tgId, e.getMessage());
            return List.of();
        }
    }

    public void getAdsByCategories() {
        List<String> categoriesByTgId = getCategoriesByTgId(hashTgId);
        StringBuilder adBuilder = new StringBuilder();
        for (String cat : categoriesByTgId) {
            List<Map<String, Object>> ads = webClient.get()
                    .uri("https://lakepay.ru/ad_category/{category}", cat)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {})
                    .block();

            if (ads == null || ads.isEmpty()) {
                continue;
            }

            adBuilder.append("\n*Объявление в категории:* ").append(cat).append("\n");
            boolean hasActiveAds = false;

            for (Map<String, Object> ad : ads) {
                Boolean isSold = (Boolean) ad.getOrDefault("sold", false);
                if (!isSold) {
                    adBuilder.append(formatAdForTelegram(ad)).append("\n");
                    hasActiveAds = true;
                }
            }

            if (!hasActiveAds) {
                adBuilder.append("Нет активных объявлений\n");
            }
        }
        String finalMessage = adBuilder.toString().trim();
        if (finalMessage.isEmpty()) {
            service.sendMessage(String.valueOf(hashTgId), "В ваших подписках пока что нет доступных объявлений");
        } else {
            service.sendMessage(String.valueOf(hashTgId), finalMessage);
        }
    }

    private String formatAdForTelegram(Map<String, Object> ad) {
        String price = ad.containsKey("price") && ad.get("price") != null ?
                ad.get("price") + "USDT" : "уточните у продавца";
        boolean isSold = ad.containsKey("sold") && Boolean.TRUE.equals(ad.get("sold"));
        String status = isSold ? "🔴 Продано" : "🟢 В продаже";

        return String.format("""
                        🎮 *Продаётся аккаунт*
                        🆔 *ID объявления:* %s
                        💬 *Заголовок:* %s
                        🕒 *Информация:* %s
                        👁  *Просмотров:* %s
                        📅 *Дата публикации:* %s
                        📦 *В наличии:* %s шт.
                        💰 *Цена:* %s
                        📌 *Категория:* %s
                        %s
                        """,
                ad.getOrDefault("id", "null"),
                ad.getOrDefault("title", "не указан"),
                ad.getOrDefault("body", "нет описания"),
                ad.getOrDefault("countOfViews", 0),
                ad.getOrDefault("dateOfPush", "не указана"),
                ad.getOrDefault("quantity", 0),
                price,
                ad.getOrDefault("category", "не указана"),
                status
        );
    }
}