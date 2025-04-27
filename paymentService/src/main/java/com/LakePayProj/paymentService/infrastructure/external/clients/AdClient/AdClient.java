package com.LakePayProj.paymentService.infrastructure.external.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "ad-service", url = "http://localhost:8081/api/ads")
public interface AdClient {

    @GetMapping("/{adId}/price")
    BigDecimal getAdPrice(@PathVariable("adId") BigDecimal adId);

    BigDecimal getAdPrice(Long adId);
}
