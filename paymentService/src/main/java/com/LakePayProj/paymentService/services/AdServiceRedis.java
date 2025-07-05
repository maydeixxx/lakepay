package com.LakePayProj.paymentService.services;

import com.LakePayProj.paymentService.exceptions.AdNotFoundException;
import com.LakePayProj.paymentService.models.DTOs.AdUpdateDto;
import com.LakePayProj.paymentService.models.redis.AdRedis;
import com.LakePayProj.paymentService.repos.AdRepoRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AdServiceRedis {

    private final AdRepoRedis adRepoRedis;

    public void saveAd(AdRedis adRedis) {
        adRepoRedis.save(adRedis);
    }

    @Cacheable(value = "ads", key = "#adId")
    public Optional<AdRedis> getAdById(Long adId) {
        return adRepoRedis.findById(adId);
    }

    public void updateCacheAd(Long adId, AdUpdateDto adUpdateDto, String field) {
        AdRedis ad = adRepoRedis.findById(adId).orElseThrow(
                () -> new AdNotFoundException(String.format("Ad by id {%s} not found", adId))
        );

        switch (field) {
            case "price" -> ad.setPrice(adUpdateDto.getPrice());
            case "login" -> ad.setLogin(adUpdateDto.getLogin());
            case "password" -> ad.setPassword(adUpdateDto.getPassword());
            case "sellerId" -> ad.setSellerId(adUpdateDto.getSellerId());
        }

        adRepoRedis.save(ad);
    }

}
