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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Void> saveAd(@RequestBody AdDto adDto) {
        service.saveAd(mapper.adDtoToDomain(adDto));
        producer.sendNewAd(adDto.getCategory(), adDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/all_ads")
    public ResponseEntity<List<AdDto>> findAllAds() {
        List<AdDto> ads = service.findAllAds()
                .stream()
                .map(mapper::adDomainToDto)
                .toList();
        return new ResponseEntity<>(ads, HttpStatus.OK);
    }

    @DeleteMapping("/ad_delete/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        service.deleteAdById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/ad_category/{category}")
    public ResponseEntity<List<AdDto>> getAdByCategory(@PathVariable String category) {
        List<AdDto> ads = service.findAdsByCategory(category)
                .stream()
                .map(mapper::adDomainToDto)
                .toList();
        return new ResponseEntity<>(ads, HttpStatus.OK);
    }

    @GetMapping("/ad_credentials/{id}")
    public ResponseEntity<Map<String, String>> getAdCredentials(@PathVariable Long id) {
        Ad ad = service.findAdById(id);
        Map<String, String> credentials = mapper.getDataFromAd(ad);
        return ResponseEntity.ok(credentials);
    }

    @GetMapping("/ad_id/{id}")
    public ResponseEntity<AdDto> findAdById(@PathVariable Long id) {
        AdDto adDto = mapper.adDomainToDto(service.findAdById(id));
        return new ResponseEntity<>(adDto, HttpStatus.OK);
    }

    @PatchMapping("update_ad/{id}")
    public ResponseEntity<Void> updateAd(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        service.updateAd(id, updates);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
