package com.LakePayProj.notificationService.kafka;

import com.LakePayProj.notificationService.tgBot.TelegramService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramConsumer {
    private final TelegramService service;
    private final WebClient webClient;
    private Long hashTgId;

    @KafkaListener(topics = "usersLog", groupId = "user-notifications")
    public void sendNewUser(ConsumerRecord<String, String> record) {
        String chatId = record.key();
        String message = record.value();

        service.sendMessage(chatId, message);
    }

    public List<String> getSubScribedUsers(String category) {
        try {
            return webClient.get()
                    .uri("https://lakepay.ru/user_category/{category}", category)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .onStatus(status -> status.isError(), response -> {
                        log.error("Ошибка при запросе подписчиков категории {}: {}", category, response.statusCode());
                        return Mono.error(new RuntimeException("Сервер вернул ошибку: " + response.statusCode()));
                    })
                    .bodyToFlux(new ParameterizedTypeReference<Map<String, Object>>() {})
                    .map(userMap -> String.valueOf(userMap.get("chatId")))
                    .collectList()
                    .block();
        } catch (Exception e) {
            log.error("Не удалось получить подписчиков категории {}: {}", category, e.getMessage());
            return List.of();
        }
    }

    public List<String> getCategoriesByTgId(Long tgId) {
        return webClient.get()
                .uri("https://lakepay.ru/categoriesById/{tgId}", tgId)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {
                })
                .block();
    }

    @KafkaListener(topics = "myAds", groupId = "user-notifications")
    public void subAds(ConsumerRecord<String, String> record) {
        String tgId = record.value();
        List<String> categoriesByTgId = getCategoriesByTgId(Long.valueOf(tgId));
        StringBuilder message = new StringBuilder();
        message.append("*Ваши подписки*\n");
        categoriesByTgId.forEach(ad -> message.append(ad).append("\n"));
        service.sendMessage(tgId, message.toString().trim());
    }

    @KafkaListener(topics = "availableAds", groupId = "user-notifications")
    public void saveTgId(ConsumerRecord<String, String> record) {
        hashTgId = Long.valueOf(record.value());
        getAdsByCategories();
    }

    public void getAdsByCategories() {
        List<String> categoriesByTgId = getCategoriesByTgId(hashTgId);
        StringBuilder adBuilder = new StringBuilder();
        for (String cat : categoriesByTgId) {
            List<Map<String, Object>> ads = (webClient.get()
                    .uri("https://lakepay.ru/ad_category/{category}", cat)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    })
                    .block());

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
            service.sendMessage(String.valueOf(hashTgId), "В ваших подписках пока что нет " +
                    "досупных объявлений");
        } else {
            service.sendMessage(String.valueOf(hashTgId), finalMessage);
        }
    }

    private String formatAdForTelegram(Map<String, Object> ad) {
        String price = ad.containsKey("price") && ad.get("price") != null ?
                ad.get("price") + "₽" : "уточните у продавца";
        boolean isSold = ad.containsKey("sold") && Boolean.TRUE.equals(ad.get("sold"));
        String status = isSold ? "🔴 Продано" : "🟢 В продаже";

        return String.format("""
                        🎮 *Продаётся аккаунт*
                        💬 *Заголовок:* %s
                        🕒 *Информация:* %s
                        👁  *Просмотров:* %s
                        📅 *Дата публикации:* %s
                        📦 *В наличии:* %s шт.
                        💰 *Цена:* %s
                        📌 *Категория:* %s
                        %s
                        """,
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

    @KafkaListener(topics = "ads", groupId = "ads-sub")
    public void sendNewAds(ConsumerRecord<String, String> record) {
        List<String> subScribedUsers = getSubScribedUsers(record.key());
        subScribedUsers.forEach(user -> service.sendMessage(user, record.value()));
    }
}