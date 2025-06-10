package com.LakePayProj.adService.api.Controllers;

import com.LakePayProj.adService.api.DTOs.AdDto;
import com.LakePayProj.adService.application.interfaces.mappers.IAdMapper;
import com.LakePayProj.adService.application.services.AdService;
import com.LakePayProj.adService.application.services.kafka.AdProducer;
import com.LakePayProj.adService.domain.Ad;
import com.LakePayProj.adService.infrastructure.AdEntity;
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
            if (adDto.getPrice() == null || adDto.getPrice().compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest().body("Price cannot be null or negative");
            }
            producer.sendNewAd(adDto.getCategory(), ad);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/all_ads")
    public ResponseEntity<?> findAllAds() {
        try {
            List<AdDto> ads = service.findAllAds()
                    .stream()
                    .map(mapper::adDomainToDto)
                    .toList();
            if (ads.isEmpty()) {
                return ResponseEntity.badRequest().body("There are no ads :(");
            }
            return ResponseEntity.ok(ads);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/personal_ads")
    public ResponseEntity<?> findPersonalAds() {
        String sellerId = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        try {
            List<AdDto> ads = service.findAdsBySellerId(Long.valueOf(sellerId))
                    .stream()
                    .map(mapper::adDomainToDto)
                    .toList();
            if (ads.isEmpty()) {
                return ResponseEntity.badRequest().body("There are no ads :(");
            }
            return ResponseEntity.ok(ads);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @DeleteMapping("/ad_delete/{id}")
    public ResponseEntity<?> deleteAd(@PathVariable Long id) {
        try {
            service.deleteAdById(id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/ad_category/{category}")
    public ResponseEntity<?> getAdByCategory(@PathVariable String category) {
        try {
            List<AdDto> ads = service.findAdsByCategory(category)
                    .stream()
                    .map(mapper::adDomainToDto)
                    .toList();
            if (ads.isEmpty()) {
                return ResponseEntity.badRequest().body("There are no ads :(");
            }
            return ResponseEntity.ok(ads);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/ad_id/{id}")
    public ResponseEntity<?> findAdById(@PathVariable Long id) {
        try {
            AdDto adDto = mapper.adDomainToDto(service.findAdById(id));
            if (adDto == null) {
                return ResponseEntity.badRequest().body("There is no ad with id = {" + id + "}");
            }
            return ResponseEntity.ok(adDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }

    }

    @PatchMapping("update_ad/{id}")
    public ResponseEntity<?> updateAd(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        try {
            service.updateAd(id, updates);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}
