package com.LakePayProj.adService.application.services.kafka;

import com.LakePayProj.adService.api.DTOs.AdDto;
import lombok.AllArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AdProducer {
    private final KafkaTemplate<String, String> template;

    private String formatAdForTelegram(AdDto ad) {
        String price = ad.getPrice() != null ? ad.getPrice() + "USDT" : "уточните у продавца";
        String status = ad.getSold() ? "🔴 Продано" : "🟢 В продаже";

        return String.format("""
                        🎮 *Продаётся аккаунт*
                        🆔 *Id объявления:* %s
                        💬 *Заголовок:* %s
                        🕒 *Информация:* %s
                        👁  *Просмотров:* %s
                        📅 *Дата публикации:* %s
                        📦 *В наличии:* %s шт.
                        💰 *Цена:* %s
                        📌 *Категория:* %s
                        %s
                        """,
                ad.getId(),
                ad.getTitle(),
                ad.getBody(),
                ad.getCountOfViews(),
                ad.getDateOfPush(),
                ad.getQuantity(),
                price,
                ad.getCategory(),
                status
        );
    }

    public void sendNewAd(String category, AdDto ad) {
        String info = formatAdForTelegram(ad);
        template.send("ads", category, info);
    }
}
