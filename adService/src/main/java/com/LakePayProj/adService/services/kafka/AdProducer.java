package com.LakePayProj.adService.services.kafka;

import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.exceptions.KafkaException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AdProducer {
    private final KafkaTemplate<String, String> template;

    private String formatAdForTelegram(Ad ad) {
        String price = ad.getPrice() != null ? ad.getPrice() + " USDT" : "уточните у продавца";
        String status = ad.getSold() ? "🔴 Продано" : "🟢 В продаже";

        return String.format("""
                        🎮 *Продаётся аккаунт*
                        💬 *Заголовок:* %s
                        🕒 *Информация:* %s
                        📅 *Дата публикации:* %s
                        💰 *Цена:* %s
                        📌 *Категория:* %s
                        %s
                        """,
                ad.getTitle(),
                ad.getBody(),
                ad.getDateOfPush(),
                price,
                ad.getCategory(),
                status
        );
    }

    public void sendNewAd(String category, Ad ad) {
        String info = formatAdForTelegram(ad);
        try {
            template.send("ads", category, info);
        } catch (Exception e) {
            throw new KafkaException("error while using kafka", e);
        }
    }
}
