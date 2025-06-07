package com.LakePayProj.notificationService.kafka;

import com.LakePayProj.notificationService.models.UserAdResponse;
import com.LakePayProj.notificationService.tgBot.TelegramService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {
    private final TelegramService service;
    private final ObjectMapper objectMapper;
    private final TelegramProducer producer;
    private final List<String> cacheSubscribedUsers = new ArrayList<>();
    private final ConcurrentHashMap<String, UserAdResponse> responseStore = new ConcurrentHashMap<>();

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
            message.append("Успешная покупка объявления 🆔 ").append(adId).append("\n*Данные от аккаунта*\n")
                    .append("login: ").append(login).append("\n")
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
            String chatId = data.get("chatId").toString();
            log.info("chatId = {}", chatId);
            String amount = data.get("amount").toString();
            String asset = data.get("currency").toString();
            String balance = data.get("balance").toString();
            StringBuilder message = new StringBuilder();
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
        Long hashTgId = Long.valueOf(record.value());
        producer.getCategoriesByTgId(hashTgId);
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
    public void handleSuccessfulWithdraw(ConsumerRecord<String, String> record) {
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
            log.info("Уведомление о выводе отправлено: userId={}, chatId={}, amount={}, currency={}", userId, chatId, amount, currency);
        } catch (Exception e) {
            log.error("Ошибка обработки withdraw_confirmed");
        }
    }

    @KafkaListener(topics = "response_sub_users", groupId = "subscribed_users")
    public void getSubScribedUsers(ConsumerRecord<String, String> record) {
        try {
            List<Integer> chatIds = objectMapper.readValue(record.value(), List.class);
            cacheSubscribedUsers.clear();
            cacheSubscribedUsers.addAll(chatIds.stream().map(String::valueOf).toList());
            log.info("Получены подписчики: {}", cacheSubscribedUsers);
        } catch (Exception e) {
            log.error("Не удалось получить подписчиков категории {}: {}", record.value(), e.getMessage());
        }
    }

    @KafkaListener(topics = "get_categories_byTg_response", groupId = "categories_tg")
    public void getCategoriesByTgId(ConsumerRecord<String, String> record) {
        try {
            String tgId = record.key();
            List<String> categories = objectMapper.readValue(record.value(), List.class);

            UserAdResponse response = new UserAdResponse();
            response.setExpectedCategories(categories);
            responseStore.put(tgId, response);

            categories.forEach(category -> producer.getAdsByCategory(category, Long.valueOf(tgId)));
            log.info("✅ Категории для tgId {}: {}", tgId, categories);
        } catch (Exception e) {
            log.error("❌ Ошибка получения категорий для tgId {}: {}", record.key(), e.getMessage());
        }
    }

    @KafkaListener(topics = "get_ad_by_category_response", groupId = "ad_category")
    public void getAdsByCategories(ConsumerRecord<String, String> record) {
        String tgId = record.key();
        String rawMessage = record.value();

        try {
            List<Map<String, Object>> ads = objectMapper.readValue(rawMessage, new TypeReference<>() {});
            String category = ads.isEmpty() ? "unknown" : ads.get(0).get("category").toString();

            StringBuilder adBuilder = new StringBuilder();
            adBuilder.append("\n*Объявление в категории:* ").append(category).append("\n");

            boolean hasActive = false;
            for (Map<String, Object> ad : ads) {
                Boolean isSold = (Boolean) ad.getOrDefault("sold", false);
                if (!isSold) {
                    adBuilder.append(formatAdForTelegram(ad)).append("\n");
                    hasActive = true;
                }
            }
            if (!hasActive) {
                adBuilder.append("Нет активных объявлений\n");
            }

            UserAdResponse response = responseStore.get(tgId);
            if (response == null) {
                log.warn("Нет ответа для tgId = {}", tgId);
                return;
            }

            response.getCategoryMessages().put(category, adBuilder.toString());

            List<String> expected = response.getExpectedCategories();
            Set<String> received = response.getCategoryMessages().keySet();

            log.debug("Получено {} / {} категорий для tgId {}", received.size(), expected.size(), tgId);

            if (received.containsAll(expected)) {
                sendFinalAdMessage(tgId, response);
            }

        } catch (Exception e) {
            log.error("Ошибка обработки объявления для tgId {}: {}", tgId, e.getMessage());
            service.sendMessage(tgId, "Произошла ошибка при обработке объявлений.");
        }
    }

    private String formatAdForTelegram(Map<String, Object> ad) {
        String price = ad.containsKey("price") && ad.get("price") != null ?
                ad.get("price") + " USDT" : "уточните у продавца";
        boolean isSold = ad.containsKey("sold") && Boolean.TRUE.equals(ad.get("sold"));
        String status = isSold ? "🔴 Продано" : "🟢 В продаже";

        return String.format("""
                        🎮 *Продаётся аккаунт*
                        🆔 *ID объявления:* %s
                        💬 *Заголовок:* %s
                        🕒 *Информация:* %s
                        📅 *Дата публикации:* %s
                        📦 *В наличии:* %s шт.
                        💰 *Цена:* %s
                        📌 *Категория:* %s
                        %s
                        """,
                ad.getOrDefault("id", "null"),
                ad.getOrDefault("title", "не указан"),
                ad.getOrDefault("body", "нет описания"),
                ad.getOrDefault("dateOfPush", "не указана"),
                ad.getOrDefault("quantity", 0),
                price,
                ad.getOrDefault("category", "не указана"),
                status
        );
    }

    private void sendFinalAdMessage(String tgId, UserAdResponse response) {
        StringBuilder message = new StringBuilder();
        for (String category : response.getExpectedCategories()) {
            String msg = response.getCategoryMessages().getOrDefault(category, null);
            if (msg != null) {
                message.append(msg);
            }
        }

        String finalMessage = message.toString().trim();
        if (finalMessage.isEmpty()) {
            service.sendMessage(tgId, "В ваших подписках пока что нет доступных объявлений");
        } else {
            service.sendMessage(tgId, finalMessage);
        }
        log.info("Отправлено сообщение для tgId {}:\n{}", tgId, finalMessage);
        responseStore.remove(tgId);
    }
}
