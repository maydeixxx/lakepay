package com.LakePayProj.adService.application.services.kafka;

import com.LakePayProj.adService.application.interfaces.mappers.IAdMapper;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.application.interfaces.repositories.IAdRepository;
import com.LakePayProj.adService.infrastructure.AdEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {
    private final IAdRepository adRepository;

    @KafkaListener(topics = "payment_confirmed", groupId = "ad-group")
    public void handlePaymentConfirmed(Map<String, Object> message) {
        Long adId = Long.valueOf(message.get("adId").toString());
        AdEntity ad = adRepository.findAdById(adId);
        ad.setSold(true);
        adRepository.save(ad);
    }
}