package com.LakePayProj.notificationService.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramProducer {
    private final KafkaTemplate<String, String> template;

    public void sendTgAndChatId(Long tgId, Long chatId) {
        try {
            template.send("userTgChatId", String.valueOf(tgId), String.valueOf(chatId));
            log.info("Отправлено сообщение в userTgChatId: tgId={}, chatId={}", tgId, chatId);
        } catch (Exception e) {
            log.error("Ошибка при отправке в userTgChatId: tgId={}, error={}", tgId, e.getMessage());
        }
    }

    public void availableAds(Long tgId) {
        try {
            template.send("availableAds", String.valueOf(tgId), String.valueOf(tgId));
            log.info("Отправлен запрос availableAds для tgId={}", tgId);
        } catch (Exception e) {
            log.error("Ошибка при отправке в availableAds: tgId={}, error={}", tgId, e.getMessage());
        }
    }

    public void getUsers(String adCategory) {
        try {
            template.send("get_sub_users", adCategory, adCategory);
            log.info("Отправлен запрос подписчиков для категории: {}", adCategory);
        } catch (Exception e) {
            log.error("Ошибка при отправке запроса подписчиков для категории {}: {}", adCategory, e.getMessage());
        }
    }

    public void getAdsByCategory(String category, Long tgId) {
        try {
            template.send("get_ad_by_category_request", String.valueOf(tgId), category);
            log.info("Отправлен запрос объявлений для категории {} и tgId {}", category, tgId);
        } catch (Exception e) {
            log.error("Ошибка при отправке запроса объявлений для категории {} и tgId {}: {}", category, tgId, e.getMessage());
        }
    }

    public void getCategoriesByTgId(Long tgId) {
        try {
            template.send("get_categories_byTg_request", String.valueOf(tgId), String.valueOf(tgId));
            log.info("Отправлен запрос категорий для tgId: {}", tgId);
        } catch (Exception e) {
            log.error("Ошибка при отправке запроса категорий для tgId {}: {}", tgId, e.getMessage());
        }
    }

    public void getUserData(Long id) {
        template.send("get_user_data_by_id_telegram_request", id.toString(), id.toString());
        log.info("Отправлено сообщение в get_user_data_by_id_request userId = {}", id);
    }
}