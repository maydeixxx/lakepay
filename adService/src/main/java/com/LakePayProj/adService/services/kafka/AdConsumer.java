package com.LakePayProj.adService.services.kafka;

import com.LakePayProj.adService.DTOs.AdDtoUpdate;
import com.LakePayProj.adService.exceptions.AdNotFoundException;
import com.LakePayProj.adService.exceptions.KafkaException;
import com.LakePayProj.adService.repositories.IAdRepository;
import com.LakePayProj.adService.services.AdService;
import com.LakePayProj.adService.entity.AdEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdConsumer {
    private final IAdRepository adRepository;
    private final AdService adService;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;

    @KafkaListener(topics = "get_ad_data_request", groupId = "AD_MONEY")
    public void sendAdToPaymentService(ConsumerRecord<String, String> record) {
        if (record.partition() == 0) {
            try {
                log.info("Получено сообщение в get_ad_data_request partition=0: key={}, value={}", record.key(), record.value());
                Long adId = Long.parseLong(record.value());
                AdEntity adEntity = adRepository.findAdById(adId).orElseThrow(() -> new AdNotFoundException(String.format("Ad %s not found", adId)));
                String sellerId = adEntity.getSellerId().toString();
                String response = objectMapper.writeValueAsString(Map.of(
                        "adId", adId,
                        "sellerId", sellerId
                ));
                template.send("get_ad_data_response", 0, adId.toString(), response);
                log.info("Отправлен sellerId в paymentService. SellerId = {}", sellerId);
            } catch (Exception e) {
                throw new KafkaException("error while using kafka", e);
            }
        } else if (record.partition() == 1) {
            log.info("Получено сообщение в get_ad_data_request partition=1: key={}, value={}", record.key(), record.value());
            Long adId = Long.parseLong(record.value());
            AdEntity adEntity = adRepository.findAdById(adId).orElseThrow(() -> new AdNotFoundException(String.format("Ad %s not found", adId)));
            log.info(adEntity.toString());
            BigDecimal price = adEntity.getPrice();
            String login = adEntity.getLogin();
            String password = adEntity.getPassword();
            try {
                String response = objectMapper.writeValueAsString(Map.of(
                        "price", price,
                        "login", login,
                        "password", password
                ));
                template.send("get_ad_data_response", 1, adId.toString(), response);
            } catch (Exception e) {
                throw new KafkaException("error while using kafka", e);
            }
        }
    }

    @KafkaListener(topics = "get_ad_by_category_request", groupId = "ad_category")
    public void responseAdByCategory(ConsumerRecord<String, String> record) {
        String tgId = record.key();
        String category = record.value();
        List<AdEntity> adsByCategory = adRepository.findAdsByCategory(category).orElseThrow(() -> new AdNotFoundException(String.format("Ad by category [%s] not found", category)));
        try {
            List<Map<String, Object>> ads = new ArrayList<>();
            for (AdEntity ad : adsByCategory) {
                ads.add(convertAdToMap(ad));
                log.info("Converted ad: {}", ads);
            }
            String response = objectMapper.writeValueAsString(ads);
            template.send("get_ad_by_category_response", tgId, response);
            log.info("Отправлен ответ для категории {} и tgId {}", category, tgId);
        } catch (Exception e) {
            throw new KafkaException("error while using kafka", e);
        }
    }

    @KafkaListener(topics = "update_ad_data", groupId = "update_ad")
    public void updateAd(ConsumerRecord<String, String> record) {
        log.info("Новое сообщение в update_ad_data. Key ={}. Value = {}", record.key(), record.value());
        Long adId = Long.parseLong(record.key());
        AdEntity adById = adRepository.findAdById(adId).orElseThrow(() -> new AdNotFoundException(String.format("Ad %s not found", adId)));
        AdDtoUpdate update = AdDtoUpdate.builder().field("sold").sold(true).build();
        adService.updateAd(adId, update);
        log.info("Объявление {} обновлено\n {}", adId, adById);
    }

    public Map<String, Object> convertAdToMap(AdEntity ad) {
        Map<String, Object> adMap = new HashMap<>();
        adMap.put("category", ad.getCategory());
        adMap.put("id", ad.getId());
        adMap.put("title", ad.getTitle());
        adMap.put("price", ad.getPrice());
        adMap.put("body", ad.getBody());
        adMap.put("dateOfPush", ad.getDateOfPush());
        adMap.put("sold", ad.getSold());
        return adMap;
    }
}