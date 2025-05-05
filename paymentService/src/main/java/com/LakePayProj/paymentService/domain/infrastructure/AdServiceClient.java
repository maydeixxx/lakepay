package com.LakePayProj.paymentService.domain.infrastructure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "adService")
public interface AdServiceClient {
    @GetMapping("/id/{id}")
    AdDto getAdById(@PathVariable("id") Long id);

    default Double getAdPrice(Long adId) {
        AdDto ad = getAdById(adId);
        return ad.getPrice(); // Предполагается, что AdDto имеет поле price
    }
}

interface AdDto {
    Long getId();
    Double getPrice();
}