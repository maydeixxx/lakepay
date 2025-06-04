package com.LakePayProj.adService.application.services.kafka;

import com.LakePayProj.adService.application.interfaces.mappers.IAdMapper;
import com.LakePayProj.adService.application.interfaces.repositories.IAdRepository;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdConsumer {
    private final IAdRepository adRepository;
    private final IAdMapper adMapper;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> template;

    @KafkaListener(topics = "payment_confirmed", groupId = "ad-group")
    public void handlePaymentConfirmed(ConsumerRecord<String, String> record) {
        try {
            Map<String, Object> data = objectMapper.readValue(record.value(), Map.class);
            Long adId = Long.valueOf(data.get("adId").toString());

            Optional<AdEntity> adEntityOptional = adRepository.findById(adId);
            if (adEntityOptional.isPresent()) {
                AdEntity adEntity = adEntityOptional.get();
                Ad ad = adMapper.adEntityToAdDomain(adEntity);
                ad.setSold(true);
                adEntity = adMapper.adDomainToEntity(ad);
                adRepository.save(adEntity);
                log.info("Объявление {} помечено как продано", adId);
            } else {
                log.warn("Объявление с adId={} не найдено", adId);
            }
        } catch (Exception e) {
            log.error("Ошибка обработки payment_confirmed: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "get_ad_data", groupId = "AD_MONEY")
    public void sendAdToPaymentService(ConsumerRecord<String, String> record) {
        if (record.partition() == 0) {
            try {
                log.info("Получено сообщение в get_ad_data partition=0: key={}, value={}", record.key(), record.value());
                Long adId = Long.parseLong(record.value());
                AdEntity adEntity = adRepository.findAdById(adId);
                String sellerId = adEntity.getSellerId().toString();
                String response = objectMapper.writeValueAsString(Map.of(
                        "adId", adId,
                        "sellerId", sellerId
                ));
                template.send("get_ad_data", 1, adId.toString(), response);
            } catch (Exception e) {
                log.error(e.getMessage());
            }
        }
    }
}