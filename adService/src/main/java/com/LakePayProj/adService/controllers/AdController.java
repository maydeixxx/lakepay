package com.LakePayProj.adService.controllers;

import com.LakePayProj.adService.DTOs.AdDto;
import com.LakePayProj.adService.DTOs.AdDtoUpdate;
import com.LakePayProj.adService.DTOs.ApiResponse;
import com.LakePayProj.adService.mappers.IAdMapper;
import com.LakePayProj.adService.services.AdService;
import com.LakePayProj.adService.services.kafka.AdProducer;
import com.LakePayProj.adService.domain.Ad;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class AdController {
    private final AdService service;
    private final IAdMapper mapper;
    private final AdProducer producer;

    @PostMapping("/save_ad")
    public ResponseEntity<?> saveAd(@RequestBody AdDto adDto) {
        try {
            Ad ad = mapper.adDtoToDomain(adDto);
            ad.setDateOfPush(LocalDate.now());
            service.saveAd(ad);
            if (ad.getPrice() == null || ad.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body("Price cannot be null or negative");
            }
            producer.sendNewAd(ad.getCategory(), ad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/delete_my_ad/{id}")
    public ResponseEntity<?> deleteMyAd(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        Ad adById = service.findAdById(id);
        Long sellerId = adById.getSellerId();
        Long userId = Long.parseLong(data.get("userId").toString());
        if (sellerId.equals(userId)) {
            service.deleteAdById(id);
            return ResponseEntity.ok(ApiResponse.builder()
                    .success(true)
                    .message(String.format("Successfully deleted ad(id: %s)", id))
                    .build()
            );
        } else {
            return ResponseEntity.badRequest().body("Вы не можете удалить чужое объявление");
        }
    }

    @GetMapping("/all_ads")
    public ResponseEntity<?> findAllAds() {
        List<AdDto> ads = service.findAllAds()
                .stream()
                .filter(ad -> ad.getSold().equals(false))
                .map(mapper::adDomainToDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("All ads")
                .data(ads)
                .build()
        );
    }

    @GetMapping("/personal_ads")
    public ResponseEntity<?> findPersonalAds() {
        String sellerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        List<AdDto> ads = service.findAdsBySellerId(Long.valueOf(sellerId))
                .stream()
                .filter(ad -> ad.getSold().equals(false))
                .map(mapper::adDomainToDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message("Your ads successfully found")
                .data(ads)
                .build()
        );
    }

    @DeleteMapping("/ad_delete/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id) {
        service.deleteAdById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/ad_category/{category}")
    public ResponseEntity<?> getAdByCategory(@PathVariable String category) {
        List<AdDto> ads = service.findAdsByCategory(category)
                .stream()
                .filter(ad -> ad.getSold().equals(false))
                .map(mapper::adDomainToDto)
                .toList();

        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("Ads by category(%s) successfully found", category))
                .data(ads)
                .build()
        );
    }

    @GetMapping("/ad_id/{id}")
    public ResponseEntity<?> findAdById(@PathVariable Long id) {
        AdDto adDto = mapper.adDomainToDto(service.findAdById(id));
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("Ad %s found", id))
                .data(adDto)
                .build()
        );
    }

    @PutMapping("update_ad/{id}")
    public ResponseEntity<?> updateAd(@PathVariable Long id, @RequestBody AdDtoUpdate updates) {
        service.updateAd(id, updates);
        return ResponseEntity.ok(ApiResponse.builder()
                .success(true)
                .message(String.format("Ad %s successfully updated", id))
        );
    }
}
