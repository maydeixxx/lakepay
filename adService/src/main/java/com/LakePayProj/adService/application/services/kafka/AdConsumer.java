package com.LakePayProj.adService.application.services.kafka;

import com.LakePayProj.adService.application.interfaces.mappers.IAdMapper;
import com.LakePayProj.adService.application.interfaces.repositories.IAdRepository;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdConsumer {
    private final IAdRepository adRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;

    @KafkaListener(topics = "get_ad_data_request", groupId = "AD_MONEY")
    public void sendAdToPaymentService(ConsumerRecord<String, String> record) {
        try {
            log.info("Получено сообщение в get_ad_data partition=0: key={}, value={}", record.key(), record.value());
            Long adId = Long.parseLong(record.value());
            AdEntity adEntity = adRepository.findAdById(adId);
            String sellerId = adEntity.getSellerId().toString();
            String response = objectMapper.writeValueAsString(Map.of(
                    "adId", adId,
                    "sellerId", sellerId
            ));
            template.send("get_ad_data_response", adId.toString(), response);
            log.info("Отправлен sellerId в paymentService. SellerId = {}", sellerId);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }

    @KafkaListener(topics = "get_ad_by_category_request", groupId = "ad_category")
    public void responseAdByCategory(ConsumerRecord<String, String> record) {
        String tgId = record.key();
        String category = record.value();
        List<AdEntity> adsByCategory = adRepository.findAdsByCategory(category);
        try {
            List<Map<String, Object>> ads = new ArrayList<>();
            for (AdEntity ad : adsByCategory) {
                ads.add(convertAdToMap(ad));
                log.info("Converted ad: {}", ads);
            }
            String response = objectMapper.writeValueAsString(ads);
            template.send("get_ad_by_category_response", tgId, response);
            log.info("Отправлен ответ для категории {} и tgId {}", category, tgId);
        } catch (JsonProcessingException e) {
            log.error("Ошибка сериализации для категории {}: {}", category, e.getMessage());
        }
    }

    public Map<String, Object> convertAdToMap(AdEntity ad) {
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("category", ad.getCategory().toString());
        adMap.put("id", ad.getId());
        adMap.put("title", ad.getTitle());
        adMap.put("price", ad.getPrice());
        adMap.put("body", ad.getBody());
        adMap.put("dateOfPush", ad.getDateOfPush());
        adMap.put("quantity", ad.getQuantity());
        adMap.put("sold", ad.getSold());
        return adMap;
    }
}