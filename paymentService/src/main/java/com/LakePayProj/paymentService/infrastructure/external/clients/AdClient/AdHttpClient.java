package com.LakePayProj.paymentService.infrastructure.external.clients.AdClient;

import com.LakePayProj.paymentService.application.DTO.AdDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AdHttpClient {

    private final WebClient webClient;

    public BigDecimal getAdPrice(Long adId) {
        return webClient.get()
                .uri("http://localhost:8081/id/" + adId)
                .retrieve()
                .bodyToMono(AdDto.class)
                .map(AdDto::getPrice)
                .block();
    }
}
