package com.LakePayProj.adService.api.Controllers;

import com.LakePayProj.adService.api.DTOs.AdDto;
import com.LakePayProj.adService.application.interfaces.mappers.IAdMapper;
import com.LakePayProj.adService.application.services.AdService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
@AllArgsConstructor
public class AdController {
    private final AdService service;
    private final IAdMapper mapper;

    @PostMapping("/save_ad")
    public ResponseEntity<Void> saveAd(@RequestBody AdDto adDto) {
        service.saveAd(mapper.adDtoToDomain(adDto));
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

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteAd(@PathVariable Long id) {
        service.deleteAdById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<AdDto>> getAdByCategory(@PathVariable String category) {
        List<AdDto> ads = service.findAdsByCategory(category)
                .stream()
                .map(mapper::adDomainToDto)
                .toList();
        return new ResponseEntity<>(ads, HttpStatus.OK);
    }

    @GetMapping("/id/{id}")
    public ResponseEntity<AdDto> findAdById(@PathVariable Long id) {
        AdDto adDto = mapper.adDomainToDto(service.findAdById(id));
        return new ResponseEntity<>(adDto, HttpStatus.OK);
    }
}
