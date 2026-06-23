package com.LakePayProj.notificationService.kafka;

import com.LakePayProj.notificationService.exceptions.WithdrawException;
import com.LakePayProj.notificationService.models.redis.UserRedis;
import com.LakePayProj.notificationService.services.TelegramService;
import com.LakePayProj.notificationService.services.UserRedisService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {

    private final TelegramService telegramService;
    private final ObjectMapper objectMapper;
    private final TelegramProducer producer;
    private final UserRedisService userRedisService;

    @KafkaListener(topics = "usersLog", groupId = "user-notifications")
    public void sendNewUser(ConsumerRecord<String, String> record) {
        String chatId = record.key();
        String message = record.value();
        telegramService.sendMessage(chatId, message);
        log.debug("Отправлено сообщение для chatId={}: {}", chatId, message);
    }

    @KafkaListener(topics = "ad_data", groupId = "MONEY")
    public void sendAdDataToUser(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), new TypeReference<>() {});
            String tgId = data.get("tgId").toString();
            String adId = data.get("adId").toString();
            String login = data.get("login").toString();
            String password = data.get("password").toString();
            String message =
                    "Успешная покупка объявления 🆔 " + adId + "\n*Данные от аккаунта*\n" +
                    "login: " + login + "\n" +
                    "password: " + password;
            telegramService.sendMessage(tgId, message.trim());
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "payment_created", groupId = "notification-group")
    public void handlePaymentCreated(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), new TypeReference<>() {});
            Long userId = Long.valueOf(data.get("userId").toString());
            String payUrl = data.get("payUrl").toString();
            telegramService.sendPaymentLink(userId, payUrl);
            log.info("Отправлена ссылка на оплату: userId={}, payUrl={}", userId, payUrl);
        } catch (Exception e) {
            log.error("Ошибка обработки payment_created: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "deposit_confirmed", groupId = "MONEY")
    public void handleSuccessfulDeposit(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), new TypeReference<>() {});
            data.forEach((key, value) -> log.info("Key: {}, Value: {}", key, value));
            String chatId = data.get("chatId").toString();
            log.info("chatId = {}", chatId);
            String amount = data.get("amount").toString();
            String asset = data.get("currency").toString();
            String balance = data.get("balance").toString();
            String message = "🤑Successful deposit🤑\n" +
                    "Amount: " + amount + "\n" +
                    "Asset: " + asset + "\n" +
                    "Your current balance: " + balance + "$ 💵";
            telegramService.sendMessage(chatId, message.trim());
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
            case 0, 1 -> telegramService.sendMessage(chatId, message);
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
        String category = record.key();
        producer.getUsers(category);
        try {
            Thread.sleep(2000);
            List<UserRedis> users = userRedisService.findUsersByCategory(category);
            log.info(users.toString());
            users.forEach(user -> telegramService.sendMessage(user.getChatId(), record.value()));
            log.info("Отправлены уведомления о новых объявлениях для категории {}: {} пользователей", record.key(), users);
        } catch (Exception e) {
            log.error("Error = {}", e.getMessage());
        }
    }

    @KafkaListener(topics = "withdraw_confirmed", groupId = "MONEY")
    public void handleSuccessfulWithdraw(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), new TypeReference<>() {});
            String userId = data.get("userId").toString();
            String chatId = data.get("chatId").toString();
            String amount = data.get("amount").toString();
            String currency = data.get("currency").toString();
            String balance = data.get("balance").toString();
            String message = "💸 Средства успешно выведены 💸\n" +
                    "Сумма: " + amount + "\n" +
                    "Валюта: " + currency + "\n" +
                    "Ваш текущий баланс: " + balance + "$ 💵";
            telegramService.sendMessage(chatId, message.trim());
            log.info("Уведомление о выводе отправлено: userId={}, chatId={}, amount={}, currency={}", userId, chatId, amount, currency);
        } catch (Exception e) {
            throw new WithdrawException("Failed to withdraw funds, message { " + e.getMessage() + " }");
        }
    }

    @KafkaListener(topics = "response_sub_users", groupId = "subscribed_users")
    public void getSubScribedUsers(ConsumerRecord<String, String> record) {
        try {
            List<String> chatIds = objectMapper.readValue(record.value(), new TypeReference<>() {});
            chatIds.forEach(
                    chatId -> userRedisService.saveUser(UserRedis.builder()
                    .chatId(chatId)
                    .build())
            );
            log.info("Получены подписчики: {}", chatIds);
        } catch (Exception e) {
            log.error("Не удалось получить подписчиков категории {}: {}", record.value(), e.getMessage());
        }
    }

    @KafkaListener(topics = "get_categories_byTg_response", groupId = "categories_tg")
    public void getCategoriesByTgId(ConsumerRecord<String, String> record) {
        try {
            String tgId = record.key();
            List<String> categories = objectMapper.readValue(record.value(), new TypeReference<>() {});
            userRedisService.saveUser(
                    UserRedis.builder()
                    .tgId(tgId)
                    .expectedCategories(categories)
                    .build()
            );

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
            if (ads.isEmpty()) {
                telegramService.sendMessage(tgId, "Нет объявлений для категории.");
                log.info("Нет объявлений для tgId {}", tgId);
                return;
            }

            UserRedis response = userRedisService.findUserByTgId(tgId);

            Map<String, List<Map<String, Object>>> adsByCategory = ads.stream()
                    .collect(Collectors.groupingBy(ad -> ad.getOrDefault("category", "unknown").toString()));

            for (String category : adsByCategory.keySet()) {
                StringBuilder adBuilder = new StringBuilder();
                adBuilder.append("*Объявление в категории:* ").append(category).append("\n");

                boolean hasActive = false;
                for (Map<String, Object> ad : adsByCategory.get(category)) {
                    Boolean isSold = (Boolean) ad.getOrDefault("sold", false);
                    if (!isSold) {
                        adBuilder.append(formatAdForTelegram(ad)).append("\n");
                        hasActive = true;
                    }
                }
                if (!hasActive) {
                    adBuilder.append("Нет активных объявлений\n");
                }

                String categoryMessage = adBuilder.toString().trim();
                if (!categoryMessage.isEmpty()) {
                    telegramService.sendMessage(tgId, categoryMessage);
                    log.info("Отправлено сообщение для tgId {} в категории {}:\n{}", tgId, category, categoryMessage);
                }

                response.getCategoryMessages().put(category, categoryMessage);
            }

            List<String> expected = response.getExpectedCategories();
            Set<String> received = response.getCategoryMessages().keySet();

            log.debug("Получено {} / {} категорий для tgId {}", received.size(), expected.size(), tgId);

            if (received.containsAll(expected)) {
                sendFinalAdMessage(tgId, response);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки объявления для tgId {}: {}", tgId, e.getMessage());
            telegramService.sendMessage(tgId, "Произошла ошибка при обработке объявлений.");
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
                        💰 *Цена:* %s
                        📌 *Категория:* %s
                        %s
                        """,
                ad.getOrDefault("id", "null"),
                ad.getOrDefault("title", "не указан"),
                ad.getOrDefault("body", "нет описания"),
                ad.getOrDefault("dateOfPush", "не указана"),
                price,
                ad.getOrDefault("category", "не указана"),
                status
        );
    }

    private void sendFinalAdMessage(String tgId, UserRedis response) {
        if (response.getCategoryMessages().isEmpty() || response.getCategoryMessages().values().stream().allMatch(String::isEmpty)) {
            telegramService.sendMessage(tgId, "В ваших подписках пока что нет доступных объявлений");
            log.info("Отправлено сообщение об отсутствии объявлений для tgId {}", tgId);
        }
    }
}
