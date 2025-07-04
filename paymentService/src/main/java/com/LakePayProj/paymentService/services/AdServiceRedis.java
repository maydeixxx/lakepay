package com.LakePayProj.paymentService.services;

import com.LakePayProj.paymentService.exceptions.AdNotFoundException;
import com.LakePayProj.paymentService.models.redis.AdRedis;
import com.LakePayProj.paymentService.repos.AdRepoRedis;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdServiceRedis {

    private final AdRepoRedis adRepoRedis;

    public void saveAd(AdRedis adRedis) {
        adRepoRedis.save(adRedis);
    }

    @Cacheable(value = "ads", key = "#adId")
    public AdRedis getAdById(Long adId) {
        return adRepoRedis.findById(adId).orElseThrow(
                () -> new AdNotFoundException(String.format("Ad by id %s not found", adId))
        );
    }

}
